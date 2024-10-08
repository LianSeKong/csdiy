# Proxy Lab

> 本实验是CSAPP的最后一个实验，大部分内容都可参考书内样例
> 实验主要涉及了IO、多线程、网络编程和并行等方面内容
> Web代理是一个在Web浏览器和终端服务器之间充当中间人的程序

得分： 70/70

## Part I

> 设置代理来接受传入的连接，读取和解析请求，将请求转发到web服务器，读取服务器的响应，并将这些响应转发到相应的客户端

### 1. 解析URL

> 代理服务器需要对传入连接的URL进行解析

URL的组成有如下：

1. host / hostname
2. pathname
3. port
4. protocol

> 例如 `https://github.com/LianSeKong/CSAPP_LABS?a=1&b=2`

1. host / hostname:  `github.com`
2. pathname: `/LianSeKong/CSAPP_LABS`
3. port: 80
4. protocol: `https:`
5. search: `?a=1&b=2`

#### 定义一个URL对象

``` c

#define PROTOCOL_SIZE 255
#define PORT_SIZE 6
#define HOST_SIZE 255
#define PATH_NAME_SIZE 4096
#define SEARCH_SIZE 4096

typedef struct {
    char protocol[PROTOCOL_SIZE];
    char port[PORT_SIZE];
    char host[HOST_SIZE];
    char search[SEARCH_SIZE];
    char pathname[PATH_NAME_SIZE];
} URL_OBJ, *URL_OBJ_T;

```

#### 解析URL

> 在本实验只需要解析HTTP/1.0协议和GET请求

``` c

int parse_uri(char *url, URL_OBJ_T url_obj_t) {
    // 1. 首先判断协议
    char protocol[PROTOCOL_SIZE] = "http://";
    // 协议不为HTTP，则返回-1
    if (strncasecmp(url, protocol, strlen(protocol)) != 0) {
        fprintf(stderr, "Not http protocol: %s\n", url);
        return -1;
    }
    strcpy(url_obj_t->protocol, "HTTP/1.0");
    // host www.yoojia.com:8080/comment/s-1684
    // 2. 提取HOST/HOSTNAME
    char *host = url + strlen("http://");
    
    // host不能为0
    if (strlen(host) == 0) {
        fprintf(stderr, "HOST ERROR: %s\n", url);
        return -1;
    }
    // 3. pathname的起始位置
    char *pathname = strchr(host, '/');
   
 // 4. PORT
 char *port = strchr(host, ':');
 
 // 复制端口号
 if (port == NULL) {
  // 默认端口号
  strcpy(url_obj_t->port, "80");
        // 复制HOST
        strncpy(url_obj_t->host, host, pathname - host);
 } else {
  strncpy(url_obj_t->port, port + 1, pathname - (port + 1));
        // 复制HOST
        strncpy(url_obj_t->host, host, port - host);
 }

    char *search = strchr(pathname, '?');
    if (search == NULL) {
     strcpy(url_obj_t->pathname, pathname);
        strcpy(url_obj_t->search, "");
    } else {
     strncpy(url_obj_t->pathname, pathname, search - pathname);
     strcpy(url_obj_t->search, search);
    }
    return 0;
}
```

### 转发请求

> 请求头具有多行
> 每行格式为 `KEY:VALUE`
> 每行以`\r\n`换行
> 以空行结束

实验要求

> 下面四行始终发送，且内容固定

1. HOST: 为请求端的URL_OBJ的HOST和PORT
2. User-Agent: `proxy.c 文件头部给出`
3. Connection: false
4. Proxy-Connection: false

解析请求头

``` c
// 首先以 : 字符去截取KEY， 然后进行判断，相等则返回0
int parse_key(char *hdr_line, char *key) {
    char *hdr_key = strtok(hdr_line, ":");
    return strcasecmp(hdr_key, key);
}

```

转发请求头

``` c
void forwarding_hdr(rio_t* conn_rio_ptr, int end_server_fd, URL_OBJ_T url_obj_t) {
    // 始终发送的请求头， User-Agent、Connection、Proxy-Connection, Host
    char* conn_hdr = "Connection: close\r\n";
    char* proxy_conn_hdr = "Proxy-Connection: close\r\n";
    char* eof_hdr = "\r\n";
    char* host_hdr[255];
    strcpy(host_hdr, "Host: ");
    strcat(host_hdr, url_obj_t->host);
    strcat(host_hdr, ":");
    strcat(host_hdr, url_obj_t->port);
    strcat(host_hdr, "\r\n");
    char buf[MAXLINE];
    Rio_readlineb(conn_rio_ptr, buf, MAXLINE);
    fputs(buf, stdout);

    while(strcmp(buf, eof_hdr)) {
        int flag = parse_key(buf, "User-Agent") && parse_key(buf, "Connection") && parse_key(buf, "Proxy-Connection") && parse_key(buf, "Host");
        if (flag != 0) {
            Rio_writen(end_server_fd, buf, strlen(buf));
        }
        Rio_readlineb(conn_rio_ptr, buf, MAXLINE);
        fputs(buf, stdout);
    }

    Rio_writen(end_server_fd, host_hdr, strlen(host_hdr));
    Rio_writen(end_server_fd, user_agent_hdr, strlen(user_agent_hdr));
    Rio_writen(end_server_fd, conn_hdr, strlen(conn_hdr));
    Rio_writen(end_server_fd, proxy_conn_hdr, strlen(proxy_conn_hdr));
    Rio_writen(end_server_fd, eof_hdr, strlen(eof_hdr));
}

```

转发请求

```c
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
````

## PartII 多线程

```c
void *thread(void *vargp)
{
    // 分离主进程，自动回收
    Pthread_detach(pthread_self());
    int connfd = *(int *)vargp;
    Free(vargp);
    forwarding(connfd);
    Close(connfd);
    return NULL;
}
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


```

## PartIII 缓存

1. 代理的整个缓存大小:

> 存储实际web对象的字节数
> 无关字节则忽略

``` c
 #define MAX_CACHE_SIZE 1049000 
```

2.最大web对象大小

```c
#define MAX_OBJECT_SIZE 102400
```

3.最大数据量 `MAX_CACHE_SIZE + T * MAX_OBJECT_SIZE`
4.T为连接数
5.缓存策略： `实现正确缓存的最简单方法是为每个活动连接分配一个缓冲区，并在从服务器接收到数据时累积数据。如果缓冲区的大小超过最大对象大小，则可以丢弃缓冲区。如果在超过最大对象大小之前读取了整个web服务器的响应，则可以缓存该对象。`
6.驱逐策略： `LRU`
7.可以多个读者，但只能一个写者

```c
#define MAX_CACHE_SIZE 1049000  // 最大缓存
#define MAX_OBJECT_SIZE 102400  // 最大对象缓存
#define MAX_CAHCE_NUM 10        //  最大缓存数量
#define MAX_READ_NUM 10   // 最大同时读者数量
sem_t mutex; // 互斥锁，用于防止竞争
sem_t w; // 写锁
sem_t r; // 读锁
// 初始化缓存
void initCache(CacheList_T cache_list_ptr)
{
    cache_list_ptr->length = 0;
    Sem_init(&w, 0, 1);
    Sem_init(&r, 0, MAX_READ_NUM);
    Sem_init(&mutex, 0, 1);
}

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
        // 课本上并没对LRU的算法有详细介绍， 这个模块则参考了别人的实现方法
        // 在CS186上，详细学习了LRU算法，这个可以通过时钟算法来模拟LRU
        // 目前并未有时间来实现

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
```
