//Following: http://www.codeproject.com/Articles/412511/Simple-client-server-network-using-Cplusplus-and-W
//original: https://msdn.microsoft.com/en-us/library/windows/desktop/bb530741

// Networking libraries
#include <winsock2.h>
#include <Windows.h>
#include <ws2tcpip.h>
#include <stdio.h>
#include "stdafx.h"
#include <string>
#include <iostream>
#include <fstream>
#include <map>

// size of our buffer
#define DEFAULT_BUFLEN 512

// unique id of the metrology machine (corresponds to the machine_id stored on the server)
#define MACHINE_ID "1"												//<--- CONFIGURE MACHINE ID HERE!

// address and port to connect sockets through
#define SERVER_ADDRESS "127.0.0.1"									//<--- CONFIGURE ADDRESS HERE!
#define DEFAULT_PORT "6881"											//<--- CONFIGURE PORT HERE!

// Need to link with Ws2_32.lib, Mswsock.lib, and Advapi32.lib
#pragma comment (lib, "Ws2_32.lib")
#pragma comment (lib, "Mswsock.lib")
#pragma comment (lib, "AdvApi32.lib")

typedef std::map<std::string, std::string> ConfigData;

class ServerConnection
{
private:
	ConfigData readConfigFile(void);

public:

	// for error checking function calls in Winsock library
    int iResult;

    // socket for client to connect to server
    SOCKET ConnectSocket;

    // constructor/destructor
    ServerConnection(void);
    ~ServerConnection(void);

	int sendMessage(char * message, int messageSize);
	int receiveMessage(char * buffer, int bufSize);
};

