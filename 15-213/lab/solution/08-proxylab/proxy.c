#include <stdio.h>
#include "csapp.h"

/* Recommended max cache and object sizes */
#define MAX_CACHE_SIZE 1049000
#define MAX_OBJECT_SIZE 102400
#define MAX_CAHCE_NUM 10
#define MAX_READ_NUM 10
#define PROTOCOL_SIZE 255
#define PORT_SIZE 6
#define HOST_SIZE 255
#define PATH_NAME_SIZE 4096
#define SEARCH_SIZE 4096
sem_t mutex; // 互斥锁，用于防止竞争
sem_t w;     // 写锁
sem_t r;     // 读锁
/* You won't lose style points for including this long line in your code */
static const char *user_agent_hdr = "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:10.0.3) Gecko/20120305 Firefox/10.0.3\r\n";

typedef struct
{
    char protocol[PROTOCOL_SIZE];
    char port[PORT_SIZE];
    char host[HOST_SIZE];
    char search[SEARCH_SIZE];
    char pathname[PATH_NAME_SIZE];
} URL_OBJ, *URL_OBJ_T;

// MAX_CACHE_SIZE + T * MAX_OBJECT_SIZE
typedef struct
{
    char url[MAXLINE];          // 一个URL对应一个数据
    char data[MAX_OBJECT_SIZE]; // 缓存的数据
    unsigned int length;        // 数据大小
    unsigned int count;         // 被使用过的次数， 读写都算, 用于LRU
    /* data */
} CacheItem, *CacheItem_T;
// 缓存
typedef struct
{
    CacheItem list[MAX_CAHCE_NUM];
    unsigned int length; // 使用了多少个缓存
} CacheList, *CacheList_T;

CacheList cache_list;

void initCache(CacheList_T cache_list_ptr);
void write_cache(CacheList_T cache_list_ptr, CacheItem_T cache_item_ptr);
CacheItem *read_cache(CacheList_T ptr_cache, char *url);
int parse_uri(char *url, URL_OBJ_T ptr);
int parse_key(char *hdr_line, char *key);
void forwarding_hdr(rio_t *conn_rio_ptr, int end_server_fd, URL_OBJ_T url_obj_t);
void forwarding(int connect_fd);
void *thread(void *vargp);

/**
 * 您的代理应在命令行中指定的端口上监听传入连接
 * 一切皆文件
 * */

int main(int argc, char **argv)
{
    // 规范命令行参数
    if (argc != 2)
    {
        fprintf(stderr, "usage: %s <port>\n", argv[0]);
        exit(1);
    }
    // 初始化缓存
    initCache(&cache_list);
    // 客户端发起的链接文件描述符， 代理服务器的链接文件描述符
    int proxy_fd;
    char hostname[MAXLINE], port[MAXLINE];
    struct sockaddr_storage client_addr; // 兼容socket地址
    socklen_t client_len = sizeof(client_addr);
    proxy_fd = Open_listenfd(argv[1]); // 代理服务器的文件描述符
    while (1)
    {
        // 阻塞，等待连接，转换socket地址为通用结构
        int *connect_fd = (int *)Malloc(sizeof(int));
        *connect_fd = Accept(proxy_fd, (SA *)&client_addr, &client_len);
        // 打印对应的连接客户端信息
        Getnameinfo((SA *)&client_addr, client_len, hostname, MAXLINE,
                    port, MAXLINE, 0);
        printf("Accepted connection from (%s, %s)\n", hostname, port);
        pthread_t tid;
        Pthread_create(&tid, NULL, thread, (void *)connect_fd);
    }
}

void *thread(void *vargp)
{
    // 分离主进程。自动回收
    Pthread_detach(pthread_self());
    int connfd = *(int *)vargp;
    Free(vargp);
    forwarding(connfd);
    Close(connfd);
    return NULL;
}

int parse_uri(char *url, URL_OBJ_T url_obj_t)
{
    // 1. 首先判断协议
    char protocol[PROTOCOL_SIZE] = "http://";
    // 协议不为HTTP，则返回-1
    if (strncasecmp(url, protocol, strlen(protocol)) != 0)
    {
        fprintf(stderr, "Not http protocol: %s\n", url);
        return -1;
    }
    strcpy(url_obj_t->protocol, "HTTP/1.0");
    // host www.yoojia.com:8080/comment/s-1684
    // 2. 提取HOST/HOSTNAME
    char *host = url + strlen("http://");

    // host不能为0
    if (strlen(host) == 0)
    {
        fprintf(stderr, "HOST ERROR: %s\n", url);
        return -1;
    }
    // 3. pathname的起始位置
    char *pathname = strchr(host, '/');

    // 4. PORT
    char *port = strchr(host, ':');

    // 复制端口号
    if (port == NULL)
    {
        // 默认端口号
        strcpy(url_obj_t->port, "80");
        // 复制HOST
        strncpy(url_obj_t->host, host, pathname - host);
    }
    else
    {
        strncpy(url_obj_t->port, port + 1, pathname - (port + 1));
        // 复制HOST
        strncpy(url_obj_t->host, host, port - host);
    }

    char *search = strchr(pathname, '?');
    if (search == NULL)
    {
        strcpy(url_obj_t->pathname, pathname);
        strcpy(url_obj_t->search, "");
    }
    else
    {
        strncpy(url_obj_t->pathname, pathname, search - pathname);
        strcpy(url_obj_t->search, search);
    }
    return 0;
}

/**
 * 转发请求帮助函数
 * client -> proxy -> server
 * client <- proxy <- server
 */

void forwarding(int connect_fd)
{
    // HTTP请求的信息, 方法： get， url, http/1.1 http/1.0
    char method[MAXLINE], url[MAXLINE], version[MAXLINE], buf[MAXLINE];
    rio_t connect_rio, end_server_rio;
    Rio_readinitb(&connect_rio, connect_fd);

    // 此时等待客户端发送请求，挂起
    if (Rio_readlineb(&connect_rio, buf, MAXLINE) <= 0)
    {
        return; // 若为空行或者连接中断则退出
    }

    fputs(buf, stdout);
    sscanf(buf, "%s %s %s", method, url, version);
    CacheItem_T cache_item_ptr = read_cache(&cache_list, url);
    if (cache_item_ptr)
    {

        Rio_writen(connect_fd, cache_item_ptr->data, cache_item_ptr->length);
        return;
    }

    URL_OBJ url_obj;
    if (parse_uri(url, &url_obj) == -1)
    {
        return;
    }

    // 建立连接
    int end_server_fd = Open_clientfd(url_obj.host, url_obj.port);
    // 初始化Rio，绑定end_server_fd
    Rio_readinitb(&end_server_rio, end_server_fd);
    // 请求头设置
    char request_header[MAXLINE];
    strcpy(request_header, method);           // METHOD
    strcat(request_header, " ");              // METHOD
    strcat(request_header, url_obj.pathname); // METHOD  /PATHNAME
    strcat(request_header, url_obj.search);   // METHOD  /PATHNAME?A=1&B=2
    strcat(request_header, " ");              // METHOD
    strcat(request_header, url_obj.protocol); // METHOD  /PATHNAME?A=1&B=2 HTTP/1.0
    strcat(request_header, "\r\n");           // METHOD  /PATHNAME?A=1&B=2 HTTP/1.0 /r/n
    Rio_writen(end_server_fd, request_header, strlen(request_header));

    forwarding_hdr(&connect_rio, end_server_fd, &url_obj);
    // 获取响应

    // 缓存对象
    CacheItem co;
    int isMaxSize = 0;
    // 获取响应
    while (Rio_readlineb(&end_server_rio, buf, MAXLINE))
    {
        fputs(buf, stdout);
        if (isMaxSize == 0 && strlen(buf) + co.length < MAX_OBJECT_SIZE)
        {
            memcpy(co.data + co.length, buf, strlen(buf));
            co.length += strlen(buf);
        }
        else
        {
            isMaxSize = -1;
        }
        Rio_writen(connect_fd, buf, strlen(buf));
        if (strcmp(buf, "\r\n") == 0)
        {
            break;
        }
    }

    char fileBuf[MAX_OBJECT_SIZE];
    unsigned int readSize = Rio_readnb(&end_server_rio, fileBuf, MAX_OBJECT_SIZE);
    while (readSize > 0)
    {
        fputs(fileBuf, stdout);
        Rio_writen(connect_fd, fileBuf, readSize);
        if (isMaxSize == 0 && co.length + readSize < MAX_OBJECT_SIZE)
        {
            memcpy(co.data + co.length, fileBuf, readSize);
            co.length += readSize;
        }
        else
        {
            isMaxSize = -1;
        }
        readSize = Rio_readnb(&end_server_rio, fileBuf, MAX_OBJECT_SIZE);
    }

    if (isMaxSize == 0)
    {
        co.count = 1;
        strcpy(co.url, url);
        write_cache(&cache_list, &co);
    }
    Close(end_server_fd);
}

// 转发请求头
void forwarding_hdr(rio_t *conn_rio_ptr, int end_server_fd, URL_OBJ_T url_obj_t)
{
    // 始终发送的请求头， User-Agent、Connection、Proxy-Connection, Host
    char *conn_hdr = "Connection: close\r\n";
    char *proxy_conn_hdr = "Proxy-Connection: close\r\n";
    char *eof_hdr = "\r\n";
    char host_hdr[255];
    strcpy(host_hdr, "Host: ");
    strcat(host_hdr, url_obj_t->host);
    strcat(host_hdr, ":");
    strcat(host_hdr, url_obj_t->port);
    strcat(host_hdr, "\r\n");
    char buf[MAXLINE];
    Rio_readlineb(conn_rio_ptr, buf, MAXLINE);
    fputs(buf, stdout);

    while (strcmp(buf, eof_hdr))
    {
        int flag = parse_key(buf, "User-Agent") && parse_key(buf, "Connection") && parse_key(buf, "Proxy-Connection") && parse_key(buf, "Host");
        if (flag != 0)
        {
            Rio_writen(end_server_fd, buf, strlen(buf));
        }
        Rio_readlineb(conn_rio_ptr, buf, MAXLINE);
        fputs(buf, stdout);
    }

    Rio_writen(end_server_fd, host_hdr, strlen(host_hdr));
    Rio_writen(end_server_fd, (void *)user_agent_hdr, strlen(user_agent_hdr));
    Rio_writen(end_server_fd, conn_hdr, strlen(conn_hdr));
    Rio_writen(end_server_fd, proxy_conn_hdr, strlen(proxy_conn_hdr));
    Rio_writen(end_server_fd, eof_hdr, strlen(eof_hdr));
}

// 解析请求行的KEY
// key: value

int parse_key(char *hdr_line, char *key)
{
    char *hdr_key = strtok(hdr_line, ":");
    return strcasecmp(hdr_key, key);
}

// 初始化缓存
void initCache(CacheList_T cache_list_ptr)
{
    cache_list_ptr->length = 0;
    Sem_init(&w, 0, 1);
    Sem_init(&r, 0, MAX_READ_NUM);
    Sem_init(&mutex, 0, 1);
}

/**
 * 多个读取者
 * 一个写入者
 *
 * 对缓存的访问必须是线程安全的，
 * 确保缓存访问不存在竞争条件可能是这一部分实验中更有趣的方面。
 * 事实上，有一个特别要求是多个线程必须能够同时从缓存中读取。
 */
// 写入缓存
void write_cache(CacheList_T cache_list_ptr, CacheItem_T cache_item_ptr)
{

    P(&w); // 获取写互斥锁，如果正在写入，则等待。
    // 等待所有读者读取完毕
    for (size_t i = 0; i < MAX_READ_NUM; i++)
    {
        P(&r);
    }
    // 未写满缓存
    if (cache_list_ptr->length != MAX_CAHCE_NUM)
    {
        // 复制缓存
        memcpy(&cache_list_ptr->list[(cache_list_ptr->length)++], cache_item_ptr, sizeof(CacheItem));
    }
    else
    {
        // 找出最小使用数， LRU， count会存在溢出问题
        int used_c = cache_list_ptr->list[0].count;
        for (size_t i = 1; i < cache_list_ptr->length; i++)
        {
            if (used_c > cache_list_ptr->list[i].count)
            {
                used_c = cache_list_ptr->list[i].count;
            }
        }
        for (size_t i = 0; i < cache_list_ptr->length; i++)
        {
            if (used_c == cache_list_ptr->list[i].count)
            {
                // 将最近没使用的数据给替换掉
                memcpy(&cache_list_ptr->list[i], cache_item_ptr, sizeof(CacheItem));
                break;
            }
        }
    }
    // 返回互斥锁
    for (size_t i = 0; i < MAX_READ_NUM; i++)
    {
        V(&r);
    }
    V(&w); // 返回写的互斥锁
}

CacheItem_T read_cache(CacheList_T cache_list_ptr, char *url)
{
    P(&r);
    for (size_t i = 0; i < cache_list_ptr->length; i++)
    {
        if (strcmp(cache_list_ptr->list[i].url, url) == 0)
        {
            P(&mutex); // 防止多个读取相同内容时候出现竞争问题
            cache_list_ptr->list[i].count += 1;
            V(&mutex);
            V(&r);
            return &cache_list_ptr->list[i];
        }
    }
    V(&r);
    return NULL;
}