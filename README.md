
Example of using NetworkExtension and tunneling. Client using Network Extension on ios.
Server should be hosted on Linux server.

It does not support encryption, authentication and multiple clients.
Use only for educational purposes.


# Server installation

1. Create tun interface
```
sudo ip tuntap add mode tun tun0
sudo ip addr add 10.0.100.1/24 dev tun0
sudo ip link set tun0 up
```

2. Configure routing:
```
sudo sysctl -w net.ipv4.ip_forward=1
sudo ufw allow 1111
sudo iptables -A INPUT -p udp --dport 1111 -j ACCEPT
sudo iptables -A INPUT -i tun0 -j ACCEPT
sudo iptables -A FORWARD -i tun0 -j ACCEPT
sudo iptables -A FORWARD -i tun0 -o <INTERNET_INTERFACE> -m state --state RELATED,ESTABLISHED -j ACCEPT
sudo iptables -A FORWARD -i <INTERNET_INTERFACE> -o tun0 -j ACCEPT
sudo iptables -t nat -A POSTROUTING -s 10.0.100.1/24 -o <INTERNET_INTERFACE> -j MASQUERADE
```
Replace <INTERNET_INTERFACE> with your internet interface (you can get one using `ip addr show`).


4. Build and run server
```
cd ./server
sudo apt-get install libc6-dev
gcc ./tunnelExampleServer.c -o ./tunnelExample
./tunnelExample
```

