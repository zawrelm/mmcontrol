#include <winsock2.h>
#include <Windows.h>
#include <string>

class NumerexControl; // forward declaration
class ServerConnection; // forward declaration

class InputThread
{

public:
	InputThread(NumerexControl *);
	~InputThread(void);

	void receiveInstruction();

	ServerConnection* connection;

};
