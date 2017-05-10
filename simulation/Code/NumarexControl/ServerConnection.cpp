//Following: http://www.codeproject.com/Articles/412511/Simple-client-server-network-using-Cplusplus-and-W
//original: https://msdn.microsoft.com/en-us/library/windows/desktop/bb530741

#include "ServerConnection.h"

ServerConnection::ServerConnection(void)
{
	printf("Loading config.txt ...\n");
	std::string machine_no, server_ip, server_port;
	ConfigData config = readConfigFile();
	std::map<char,int>::iterator it;

	/* load machine_id */
	if(config.find("MACHINE_ID") != config.end()) {
		machine_no = config.find("MACHINE_ID")->second;
		printf("MACHINE_ID loaded\n");
	}
	else {
		machine_no = MACHINE_ID;
		printf("WARNING: MACHINE_ID not found!\n");
	}

	/* load server_ip */
	if(config.find("SERVER_IP") != config.end()) {
		server_ip = config.find("SERVER_IP")->second;
		printf("SERVER_IP loaded\n");
	}
	else {
		server_ip = SERVER_ADDRESS;
		printf("WARNING: SERVER_IP not found!\n");
	}

	/* load server_port */
	if(config.find("SERVER_PORT") != config.end()) {
		server_port = config.find("SERVER_PORT")->second;
		printf("SERVER_PORT loaded\n");
	}
	else {
		server_port = DEFAULT_PORT;
		printf("WARNING: SERVER_PORT not found!\n");
	}

	printf("Contacting server...\n");

	// create WSADATA object
    WSADATA wsaData;

    // socket
    ConnectSocket = INVALID_SOCKET;

    // holds address info for socket to connect to
    struct addrinfo *result = NULL,
                    *ptr = NULL,
                    hints;

    // Initialize Winsock
    iResult = WSAStartup(MAKEWORD(2,2), &wsaData);

    if (iResult != 0) {
        printf("WSAStartup failed with error: %d\n", iResult);
        exit(1);
    }

    // set address info
    ZeroMemory( &hints, sizeof(hints) );
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;  //TCP connection!!!

	// resolve server address and port
	const char * ip = server_ip.c_str();
	const char * port = server_port.c_str();
	iResult = getaddrinfo(ip, port, &hints, &result);

	if( iResult != 0 )
	{
		printf("getaddrinfo failed with error: %d\n", iResult);
		WSACleanup();
		exit(1);
	}

	// Attempt to connect to an address until one succeeds
	for(ptr=result; ptr != NULL ;ptr=ptr->ai_next) {

	    // Create a SOCKET for connecting to server
		ConnectSocket = socket(ptr->ai_family, ptr->ai_socktype,
			ptr->ai_protocol);

	    if (ConnectSocket == INVALID_SOCKET) {
		    printf("socket failed with error: %ld\n", WSAGetLastError());
			WSACleanup();
			exit(1);
		}

	    // Connect to server.
		iResult = connect( ConnectSocket, ptr->ai_addr, (int)ptr->ai_addrlen);

	    if (iResult == SOCKET_ERROR)
		{
			closesocket(ConnectSocket);
			ConnectSocket = INVALID_SOCKET;
			printf("The server is unreachable... did not connect");
		}
	}

	// no longer need address info for server
	freeaddrinfo(result);

	// if connection failed
	if (ConnectSocket == INVALID_SOCKET)
	{
		printf("Unable to connect to server!\n");
		WSACleanup();
		exit(1);
	}

	// SET THE MODE OF THE SOCKET TO NON-BLOCKING
/*	u_long iMode = 1;

	iResult = ioctlsocket(ConnectSocket, FIONBIO, &iMode);
	if (iResult == SOCKET_ERROR)
	{
		printf("ioctlsocket failed with error: %d\n", WSAGetLastError());
		closesocket(ConnectSocket);
		WSACleanup();
		exit(1);
	}*/

	// disable nagle algorithm (avoiding small package transfer)
    char value = 1;
    setsockopt( ConnectSocket, IPPROTO_TCP, TCP_NODELAY, &value, sizeof( value ) );

	// RECEIVE "WHO IS IT?" MESSAGE FROM SERVER AND RESPOND WITH MACHINE_ID
	char buffer[8]; // buffer to store received message
	printf("Trying to receive data from server...");

//	Sleep(50);
	iResult = receiveMessage(buffer, (int) strlen(buffer));
	if ( iResult > 0 ) {
		if(std::strcmp(buffer, "WHO")) printf("successful!\n");
		else {
			printf("received unexpected command! Exiting...");
			exit(1);
		}
	}
    else if ( iResult == 0 )
		printf("Connection closed\n");
    else {
		printf("Result: %i\n", iResult);
        printf("recv failed with error: %d\n", WSAGetLastError());
		exit(1);
	}

	char data[64]; // array to hold the data to send
	char *keyword = "ID "; // fixed keyword according to defined communication protocol
	std::strcpy(data, keyword); // copy string into message data
	const char * machine = machine_no.c_str();
	std::strcat(data, machine); // append string to message data
	std::strcat(data, "\n"); // append line break
	printf("Identifying at server with %s", data);

	iResult = sendMessage(data, (int) strlen(data));
	if (iResult == SOCKET_ERROR) {
        printf("send failed with error: %d\n", WSAGetLastError());
        closesocket(ConnectSocket);
        WSACleanup();
        exit(1);
    }

}


ServerConnection::~ServerConnection(void)
{
}


int ServerConnection::sendMessage(char * message, int messageSize)
{
    return send(ConnectSocket, message, messageSize, 0);
}


int ServerConnection::receiveMessage(char * buffer, int bufSize)
{
    return recv(ConnectSocket, buffer, bufSize, 0);
}

ConfigData ServerConnection::readConfigFile()
{
	ConfigData configValues;
    
	std::ifstream file("config.txt");
	std::string delimiter("=");

	std::string line;

	/* read in file, line by line */
	while (std::getline(file, line))
    {
		std::string key;
		std::string value;

		/* split line at delimiter, store into key and value */
		std::size_t del_pos = line.find(delimiter);
		if (del_pos!=std::string::npos)
		{
			key = line.substr(0,del_pos);
			value = line.substr(del_pos+1);

            if (key[0] == '#') continue;

            configValues[key] = value;
        }
    }

	return configValues;
}
