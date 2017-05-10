#include "stdafx.h"
#include "ModaCPP.h"
#include <process.h>	// _beginthread, _endthread
#include <iostream>
#include <sstream>
#include <string>
#include <cmath>
#include <Windows.h>

#define MYROBOTNAME	"/phx0"

class InputThread; // forward declaration

class NumerexControl
{

private:
public:
	NumerexControl(void);
	~NumerexControl(void);

	static InputThread *inputThread;

	static void runInputThread( void * );
	std::string getMachineState( void );
	void stopInputThread( void );

	void haltMachine( bool ); // can be triggered by PLC and server, param: resetClips

	void firstTouchAction(int, int, int);	// params: xMoved, yMoved, zMoved
	void checkBoundaryReached( void );
	int getContact( void );

	bool moveX(bool, bool, bool);	// params: fast, backward, move
	bool moveY(bool, bool, bool);	// params: fast, backward, move
	bool moveZ(bool, bool, bool);	// params: fast, backward, move
	
	void setXFastConnected( bool );
	void setXSlowConnected( bool );
	void setYFastConnected( bool );
	void setYSlowConnected( bool );
	void setZFastConnected( bool );
	void setZSlowConnected( bool );

	void moveXToPosition(float, bool);
	void moveYToPosition(float, bool);
	void moveZToPosition(float, bool);

/*	float moveXFast( float );
	float moveXSlow( float );
	float moveYFast( float );
	float moveYSlow( float );
	float moveZFast( float );
	float moveZSlow( float ); */

};
