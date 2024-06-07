#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <linux/if.h>
#include <linux/if_tun.h>
#include <errno.h>
#include <string.h>
#include <sys/select.h>
#include <sys/time.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define buffersize 5000

int tun_fd ;

void udpSession(int sock, struct sockaddr *clientAddr) {
    printf("Start receiving udp\r\n");
    
    char buffer[buffersize];

    int len = sizeof(struct sockaddr_in);
    int maxfd = (tun_fd > sock) ? tun_fd : sock;

    while (1) {

        fd_set rdSet;

        FD_ZERO(&rdSet);
        FD_SET(tun_fd, &rdSet);
        FD_SET(sock, &rdSet);

        int selectResult = select(maxfd + 1, &rdSet, NULL, NULL, NULL);
        if (selectResult < 0 && errno == EINTR){
            continue;
        }

        if (selectResult < 0) {
            perror("select()");
            exit(1);
        }

        if(FD_ISSET(sock, &rdSet)) {
            int bytesRead = recvfrom(sock, buffer, buffersize, 0, clientAddr, &len);
            if (bytesRead > 0) {
                printf("bytes read from sock: %d\r\n", bytesRead);
                int writeRes = write(tun_fd, buffer, bytesRead);
                printf("wrote to tun: %d\r\n", writeRes);
            } 
        }
        

        if(FD_ISSET(tun_fd, &rdSet)) {
            unsigned short bytesRead = (unsigned short)read(tun_fd, buffer, buffersize);
            if (bytesRead > 0) {
                printf("bytes read from tun: %d\r\n", bytesRead);

                int writeRes = sendto(sock, buffer, bytesRead, 0, clientAddr, len); 
                printf("wrote to sock: %d\r\n", writeRes);
            } 
        }
    }
    printf("Close session\r\n");    
}

void updServer() {
    int sock = socket(AF_INET, SOCK_DGRAM, 0);
    char* buffer = (char*) calloc(buffersize, sizeof(char));

    struct sockaddr_in servAddr, clientAddr;

    memset(&servAddr, 0, sizeof(servAddr)); 
    memset(&clientAddr, 0, sizeof(clientAddr));  

    servAddr.sin_family  = AF_INET; // IPv4 
    servAddr.sin_addr.s_addr = INADDR_ANY;
    servAddr.sin_port = htons(1111); 

    if (bind(sock, (const struct sockaddr *)&servAddr, sizeof(servAddr)) < 0) 
    { 
        printf("binderror: %d\r\n", errno);
        return;
    } 

    int len = sizeof(struct sockaddr_in);
   
    while (1) {
        int bytesRead = recvfrom(sock, buffer, buffersize, MSG_WAITALL, (struct sockaddr *) &clientAddr, 
                    &len); 
        printf("result form server %d: %s\n", clientAddr.sin_addr.s_addr, buffer);

        // run session for client ip and port which we received from recvfrom
        udpSession(sock, (struct sockaddr *) &clientAddr);
    }

    free(buffer);
}

int main() {
    // Open tun0
    tun_fd = open("/dev/net/tun", O_RDWR);
    struct ifreq ifr;
    memset(&ifr, 0, sizeof(ifr));
    ifr.ifr_flags = IFF_TUN | IFF_NO_PI;
    strcpy(ifr.ifr_name, "tun0");
    ioctl(tun_fd, TUNSETIFF, (void *)&ifr);

    // Start socket server
    updServer();
    return 0;
}
