// NumerexControl.cpp : Defines the entry point for the console application.
//
#include "NumerexControl.h"
#include "InputThread.h"
#include "ServerConnection.h"

//#define GetRandom( min, max ) ((rand() % (int)(((max) + 1) - (min))) + (min))

InputThread * NumerexControl::inputThread;

bool repeat = true;		//run condition for InputThread

int xMoving = 0; //-2 fast backwards, -1 slow backwards, 0 still, 1 slow forward, 2 fast forward
int yMoving = 0; //-2 fast backwards, -1 slow backwards, 0 still, 1 slow forward, 2 fast forward
int zMoving = 0; //-2 fast backwards, -1 slow backwards, 0 still, 1 slow forward, 2 fast forward
int contact = 0; //0 no contact, 1 approach automatically, 2 contact maintained	- WHILE AUTOMATICALLY APPROACHING, THE MACHINE SHOULD NOT ACCEPT ANY NEW ORDERS

/* variables for complex functionality "go to position" */
float xTarget = NULL;
float yTarget = NULL;
float zTarget = NULL;

/* MACHINE CONFIGURATION */
const float SPEED_FAST = 0.1f;
const float SPEED_SLOW = 0.01f;
const float LIMIT_X_MIN = -0.2f;
const float X_POS_STANDARD = 0.0f;
const float LIMIT_X_MAX = 0.2f;
const float LIMIT_Y_MIN = -0.3f;
const float Y_POS_STANDARD = -0.00001f;
const float LIMIT_Y_MAX = -0.00001f;
const float LIMIT_Z_MIN = -0.3f;
const float Z_POS_STANDARD = -0.00001f;
const float LIMIT_Z_MAX = -0.00001f;
const float PINZAS_POS_OPENED = -0.001f;
const float PINZA_X_POS_CLOSED = -0.03f;
const float PINZA_Y_POS_CLOSED = -0.04f;
const float PINZA_Z_POS_CLOSED = -0.02f;

ModaCPP::DeviceActuatingCylinder * pDevice_AXCarro, * pDevice_AXMotorGrueso, * pDevice_AXMotorFino, * pDevice_AXPinzaGrueso, * pDevice_AXPinzaFino;
ModaCPP::DeviceActuatingCylinder * pDevice_AYCarro, * pDevice_AYMotorGrueso, * pDevice_AYMotorFino, * pDevice_AYPinzaGrueso, * pDevice_AYPinzaFino;
ModaCPP::DeviceActuatingCylinder * pDevice_AZCarro, * pDevice_AZMotorGrueso, * pDevice_AZMotorFino, * pDevice_AZPinzaGrueso, * pDevice_AZPinzaFino;
ModaCPP::DeviceContact * pDevice_SXPinzaGrueso, * pDevice_SXPinzaFino, * pDevice_SYPinzaGrueso, * pDevice_SYPinzaFino, * pDevice_SZPinzaGrueso, * pDevice_SZPinzaFino;
ModaCPP::DeviceContact * pDevice_SensingHead;

NumerexControl::NumerexControl()
{

	inputThread = new InputThread(this);

}

// TODO: if server doesn't respond (SocketException?) stop all motors! <- indirectly already implemented by collision and extrem values check

int _tmain(int argc, _TCHAR* argv[])
{
	NumerexControl * control = new NumerexControl();

	//process the command line
	ModaCPP::CommandLine::ProcessCommandLine(argc,argv);
	//Connect to MODA server
	ModaCPP::Connection *pConnection=new ModaCPP::Connection(true);
	if(pConnection->Connect( ModaCPP::CommandLine::GetArgumentValue("/modaserver","127.0.0.1"),ModaCPP::CommandLine::GetArgumentValueINT("/modaport",0),false))
	{
		_cprintf("Connection ok to moda server\r\n");
		//Find the robot
		ModaCPP::RobotPHX *robot=pConnection->QueryRobotPHX(MYROBOTNAME);
		if(robot)
		{
			_cprintf("robot found\r\n");

			/* 1. INITIALIZATION - Find devices */
			pDevice_AXCarro= robot->QueryDeviceActuatingCylinder("sliderFrontal/a1/xSlider");
			pDevice_AXMotorGrueso= robot->QueryDeviceActuatingCylinder("motorFrontalGrueso/a1/xMotorGrueso");
			pDevice_AXMotorFino= robot->QueryDeviceActuatingCylinder("motorFrontalFino/a1/xMotorFino");
			pDevice_AXPinzaGrueso= robot->QueryDeviceActuatingCylinder("pinzaFrontalGrueso/a1/xPinzaGrueso");
			pDevice_AXPinzaFino= robot->QueryDeviceActuatingCylinder("pinzaFrontalFino/a1/xPinzaFino");
			pDevice_SXPinzaGrueso= robot->QueryDeviceContact("cylinder10/xSensorPinzaGrueso");
			pDevice_SXPinzaFino= robot->QueryDeviceContact("boxPinzaFrontalFino/xSensorPinzaFino");
			std::cout << "Frontal loaded\n";

			pDevice_AYCarro= robot->QueryDeviceActuatingCylinder("sliderLateral/a1/ySlider");
			pDevice_AYMotorGrueso= robot->QueryDeviceActuatingCylinder("motorLateralGrueso/a1/yMotorGrueso");
			pDevice_AYMotorFino= robot->QueryDeviceActuatingCylinder("motorLateralFino/a1/yMotorFino");
			pDevice_AYPinzaGrueso= robot->QueryDeviceActuatingCylinder("pinzaLateralGrueso/a1/yPinzaGrueso");
			pDevice_AYPinzaFino= robot->QueryDeviceActuatingCylinder("pinzaLateralFino/a1/yPinzaFino");
			pDevice_SYPinzaGrueso= robot->QueryDeviceContact("boxPinzaLateralGrueso/ySensorPinzaGrueso");
			pDevice_SYPinzaFino= robot->QueryDeviceContact("boxPinzaLateralFino/ySensorPinzaFino");
			std::cout << "Lateral loaded\n";

			pDevice_AZCarro= robot->QueryDeviceActuatingCylinder("sliderVertical/a1/zSlider");
			pDevice_AZMotorGrueso= robot->QueryDeviceActuatingCylinder("motorVerticalGrueso/a1/zMotorGrueso");
			pDevice_AZMotorFino= robot->QueryDeviceActuatingCylinder("motorVerticalFino/a1/zMotorFino");
			pDevice_AZPinzaGrueso= robot->QueryDeviceActuatingCylinder("pinzaVerticalGrueso/a1/zPinzaGrueso");
			pDevice_AZPinzaFino= robot->QueryDeviceActuatingCylinder("pinzaVerticalFino/a1/zPinzaFino");
			pDevice_SZPinzaGrueso= robot->QueryDeviceContact("boxPinzaVerticalGrueso/zSensorPinzaGrueso");
			pDevice_SZPinzaFino= robot->QueryDeviceContact("boxPinzaVerticalFino/zSensorPinzaFino");
			std::cout << "Vertical loaded\n";
			pDevice_SensingHead= robot->QueryDeviceContact("palpador1/sphere0_5mm_10g/contactSensor");
		
			if( (pDevice_AXCarro!=NULL) && (pDevice_AXMotorGrueso!=NULL) && (pDevice_AXMotorFino!=NULL) && (pDevice_AXPinzaGrueso!=NULL) && (pDevice_AXPinzaFino!=NULL) && (pDevice_SXPinzaGrueso!=NULL) && (pDevice_SXPinzaFino!=NULL)
				&& (pDevice_AYCarro!=NULL) && (pDevice_AYMotorGrueso!=NULL) && (pDevice_AYMotorFino!=NULL) && (pDevice_AYPinzaGrueso!=NULL) && (pDevice_AYPinzaFino!=NULL) && (pDevice_SYPinzaGrueso!=NULL) && (pDevice_SYPinzaFino!=NULL)
				&& (pDevice_AZCarro!=NULL) && (pDevice_AZMotorGrueso!=NULL) && (pDevice_AZMotorFino!=NULL) && (pDevice_AZPinzaGrueso!=NULL) && (pDevice_AZPinzaFino!=NULL) && (pDevice_SZPinzaGrueso!=NULL) && (pDevice_SZPinzaFino!=NULL)
				&& (pDevice_SensingHead!=NULL))
			{	//All devices are OK
				
				/* 2. INITIALIZATION - Establish initial state */
				pDevice_AXPinzaFino->GoPosition(PINZA_X_POS_CLOSED, SPEED_FAST);
				pDevice_AYPinzaFino->GoPosition(PINZA_Y_POS_CLOSED, SPEED_FAST);
				pDevice_AZPinzaFino->GoPosition(PINZA_Z_POS_CLOSED, SPEED_FAST);
				
				pConnection->Sleep(1000);
					std::cout << "Establishing initial state..." << pDevice_SensingHead->IsTutching() << pDevice_SXPinzaGrueso->IsTutching() << pDevice_SXPinzaFino->IsTutching() << pDevice_SYPinzaGrueso->IsTutching()
						<< pDevice_SYPinzaFino->IsTutching() << pDevice_SZPinzaGrueso->IsTutching() << pDevice_SZPinzaFino->IsTutching() << "\n>";

//				std::cout << "Waiting for commands... syntax: (mxg|mxf|myg|myf|mzg|mzf) <position in meters>\nOR (pxg|pxf|pyg|pyf|pzg|pzf) (0|1)\n>";
				
				//inputThread = new InputThread(); //initialize the server connection
				_beginthread( NumerexControl::runInputThread, 0, 0); //listen for incoming commands from server and execute them

				/****************************************************/
				/* 3. END OF INITIALIZATION, START OF PROGRAM LOOP! */
				/****************************************************/
				while(repeat) {

					/* check for new sensing head collision */
					if(pDevice_SensingHead->IsTutching() == 1 && contact == 0) {
						control->firstTouchAction(xMoving, yMoving, zMoving);
						std::cout << "Value of contact = " << contact << "\n";
					}
					else if(contact == 2 && !pDevice_SensingHead->IsTutching()) {
						contact = 0;
					}

					/* check for motor positions reaching their extrem values */
					control->checkBoundaryReached();

					/* send periodic status information */
					std::string s = control->getMachineState();
					char *data = new char[s.length() + 4];
					strcpy(data, "IN ");
					strcat(data, s.c_str());
					//std::cout << data;
					NumerexControl::inputThread->connection->sendMessage(data, (int) strlen(data));
					delete [] data;
					//std::cout << "State reported to server.\n";

					/* ONLY FOR TESTING: delay next run */
					//pConnection->Sleep(100);
					
				}
				std::cout << "END!\n";
				pConnection->Sleep(3000);
			}
			else
			{
				_cprintf("Device not found \r\n");
			}
			
			/* 4. DESTRUCTION - delete all devices */
			if(pDevice_AXCarro!=NULL) delete pDevice_AXCarro;
			if(pDevice_AXMotorGrueso!=NULL) delete pDevice_AXMotorGrueso;
			if(pDevice_AXMotorFino!=NULL) delete pDevice_AXMotorFino;
			if(pDevice_AXPinzaGrueso!=NULL) delete pDevice_AXPinzaGrueso;
			if(pDevice_AXPinzaFino!=NULL) delete pDevice_AXPinzaFino;
			if(pDevice_SXPinzaGrueso!=NULL) delete pDevice_SXPinzaGrueso;
			if(pDevice_SXPinzaFino!=NULL) delete pDevice_SXPinzaFino;

			if(pDevice_AYCarro!=NULL) delete pDevice_AYCarro;
			if(pDevice_AYMotorGrueso!=NULL) delete pDevice_AYMotorGrueso;
			if(pDevice_AYMotorFino!=NULL) delete pDevice_AYMotorFino;
			if(pDevice_AYPinzaGrueso!=NULL) delete pDevice_AYPinzaGrueso;
			if(pDevice_AYPinzaFino!=NULL) delete pDevice_AYPinzaFino;
			if(pDevice_SYPinzaGrueso!=NULL) delete pDevice_SYPinzaGrueso;
			if(pDevice_SYPinzaFino!=NULL) delete pDevice_SYPinzaFino;

			if(pDevice_AZCarro!=NULL) delete pDevice_AZCarro;
			if(pDevice_AZMotorGrueso!=NULL) delete pDevice_AZMotorGrueso;
			if(pDevice_AZMotorFino!=NULL) delete pDevice_AZMotorFino;
			if(pDevice_AZPinzaGrueso!=NULL) delete pDevice_AZPinzaGrueso;
			if(pDevice_AZPinzaFino!=NULL) delete pDevice_AZPinzaFino;
			if(pDevice_SZPinzaGrueso!=NULL) delete pDevice_SZPinzaGrueso;
			if(pDevice_SZPinzaFino!=NULL) delete pDevice_SZPinzaFino;

			if(pDevice_SensingHead!=NULL) delete pDevice_SensingHead;
		}
		else
		{
			_cprintf("robot not found\r\n");
		}

		/* END OF ACTUAL CODE */
		delete robot;
	}
	else
	{
		_cprintf("Unable to connect to moda server\r\n");
	}
	//Disconnect & delete
	pConnection->Disconnect();
	delete pConnection;
	_getch();
	return 0;
}

/* Listen for incoming commands from server as long as no "BYE" command has been sent.
   Machine shutdown is always induced by server */
void NumerexControl::runInputThread(void * arg) 
{ 
    while(repeat) 
    {
		inputThread->receiveInstruction();
    }
}

std::string NumerexControl::getMachineState()
{
	std::stringstream sstm;
	sstm << "0 " << (xMoving == 2 ? "1" : "0") << " 1 " << (xMoving == -2 ? "1" : "0") << " 2 " << (xMoving == 1 ? "1" : "0") << " 3 " << (xMoving == -1 ? "1" : "0")
		 << " 4 " << (yMoving == 2 ? "1" : "0") << " 5 " << (yMoving == -2 ? "1" : "0") << " 6 " << (yMoving == 1 ? "1" : "0") << " 7 " << (yMoving == -1 ? "1" : "0")
		 << " 8 " << (zMoving == 2 ? "1" : "0") << " 9 " << (zMoving == -2 ? "1" : "0") << " 10 " << (zMoving == 1 ? "1" : "0") << " 11 " << (zMoving == -1 ? "1" : "0")
		 << " 12 " << (pDevice_AXPinzaGrueso->GetPosition() < PINZA_X_POS_CLOSED+0.01f ? "1" : "0")
		 << " 13 " << (pDevice_AXPinzaFino->GetPosition() < PINZA_X_POS_CLOSED+0.01f ? "1" : "0")
		 << " 14 " << (pDevice_AYPinzaGrueso->GetPosition() < PINZA_Y_POS_CLOSED+0.01f ? "1" : "0")
		 << " 15 " << (pDevice_AYPinzaFino->GetPosition() < PINZA_Y_POS_CLOSED+0.01f ? "1" : "0")
		 << " 16 " << (pDevice_AZPinzaGrueso->GetPosition() < PINZA_Z_POS_CLOSED+0.01f ? "1" : "0")
		 << " 17 " << (pDevice_AZPinzaFino->GetPosition() < PINZA_Z_POS_CLOSED+0.01f ? "1" : "0")
		 << " 18 1 19 " << pDevice_SensingHead->IsTutching()
		 << " 20 " << pDevice_SXPinzaGrueso->IsTutching() << " 21 " << pDevice_SXPinzaFino->IsTutching() << " 22 " << pDevice_SYPinzaGrueso->IsTutching()
		 << " 23 " << pDevice_SYPinzaFino->IsTutching() << " 24 " << pDevice_SZPinzaGrueso->IsTutching() << " 25 " << pDevice_SZPinzaFino->IsTutching()
		 << " 26 " << pDevice_AXCarro->GetPosition() << " 27 " << pDevice_AYCarro->GetPosition() << " 28 " << pDevice_AZCarro->GetPosition() << "\n";

	return sstm.str();
}

void NumerexControl::stopInputThread()
{
    repeat = false;    // _endthread implied
}

void NumerexControl::haltMachine(bool resetClips)
{
	pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition());
	pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition());
	pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition());

	pDevice_AXMotorGrueso->GoPosition(pDevice_AXMotorGrueso->GetPosition());
	pDevice_AYMotorGrueso->GoPosition(pDevice_AYMotorGrueso->GetPosition());
	pDevice_AZMotorGrueso->GoPosition(pDevice_AZMotorGrueso->GetPosition());

	pDevice_AXMotorFino->GoPosition(pDevice_AXMotorFino->GetPosition());
	pDevice_AYMotorFino->GoPosition(pDevice_AYMotorFino->GetPosition());
	pDevice_AZMotorFino->GoPosition(pDevice_AZMotorFino->GetPosition());

	if(resetClips) {
		pDevice_AXPinzaFino->GoPosition(PINZA_X_POS_CLOSED, SPEED_FAST);
		pDevice_AYPinzaFino->GoPosition(PINZA_Y_POS_CLOSED, SPEED_FAST);
		pDevice_AZPinzaFino->GoPosition(PINZA_Z_POS_CLOSED, SPEED_FAST);

		pDevice_AXPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AYPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AZPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
	}

	xMoving = 0;
	yMoving = 0;
	zMoving = 0;
}

void NumerexControl::firstTouchAction(int xMoved, int yMoved, int zMoved)
{
	if(contact != 0) return;
	contact = 1;
	NumerexControl::haltMachine(true);
	std::cout << "INFO: First touch action activated!\n";

	while(pDevice_SensingHead->IsTutching()) {
		if(xMoved != 0) {
			moveX(false, (xMoved > 0), true);
			moveX(false, (xMoved > 0), false);
		}
		if(yMoved != 0) {
			moveY(false, (yMoved > 0), true);
			moveY(false, (yMoved > 0), false);
		}
		if(zMoved != 0) {
			moveZ(false, (zMoved > 0), true);
			moveZ(false, (zMoved > 0), false);
		}
	}
	
	if(xMoved != 0) {
		moveX(false, (xMoved < 0), true);
		moveX(false, (xMoved < 0), false);
	}
	if(yMoved != 0) {
		moveY(false, (yMoved < 0), true);
		moveY(false, (yMoved < 0), false);
	}
	if(zMoved != 0) {
		moveZ(false, (zMoved < 0), true);
		moveZ(false, (zMoved < 0), false);
	}

	contact = 2;
	std::cout << "INFO: First touch action finished!\n";
}

void NumerexControl::checkBoundaryReached()
{
	if((xMoving < 0 && pDevice_AXCarro->GetPosition() < LIMIT_X_MIN) || (xMoving > 0 && pDevice_AXCarro->GetPosition() > LIMIT_X_MAX)) {
		NumerexControl::moveX(true, false, false);	//STOP X
		std::cout << "INFO: Boundary x reached!\n";
	}

	if((yMoving < 0 && pDevice_AYCarro->GetPosition() < LIMIT_Y_MIN) || (yMoving > 0 && pDevice_AYCarro->GetPosition() > LIMIT_Y_MAX)) {
		NumerexControl::moveY(true, false, false);	//STOP Y
		std::cout << "INFO: Boundary y reached!\n";
	}

	if((zMoving < 0 && pDevice_AZCarro->GetPosition() < LIMIT_Z_MIN) || (zMoving > 0 && pDevice_AZCarro->GetPosition() > LIMIT_Z_MAX)) {
		NumerexControl::moveZ(true, false, false);	//STOP Z
		std::cout << "INFO: Boundary z reached!\n";
	}

	if(xTarget != NULL && abs(xTarget - pDevice_AXCarro->GetPosition()) < 0.05f) { //&& abs(xMoving) == 2
		NumerexControl::moveXToPosition(xTarget, false);	//slow down X
		std::cout << "INFO: Slowly approaching target coordinate x!\n";
	}

	if(yTarget != NULL && abs(yTarget - pDevice_AYCarro->GetPosition()) < 0.05f) {
		NumerexControl::moveYToPosition(yTarget, false);	//slow down Y
		std::cout << "INFO: Slowly approaching target coordinate y!\n";
	}

	if(zTarget != NULL && abs(zTarget - pDevice_AZCarro->GetPosition()) < 0.05f) {
		NumerexControl::moveZToPosition(zTarget, false);	//slow down Z
		std::cout << "INFO: Slowly approaching target coordinate z!\n";
	}
}

int NumerexControl::getContact()
{
	return contact;
}

bool NumerexControl::moveX(bool fast, bool backward, bool move)
{
	//std::cout << "MOVE X FAST? " << fast << ", BACKWARD? " << backward << ", MOVE? " << move << "\n";
	if(!move) {
		pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition());
		pDevice_AXMotorGrueso->GoPosition(pDevice_AXMotorGrueso->GetPosition());
		pDevice_AXMotorFino->GoPosition(pDevice_AXMotorFino->GetPosition());
		xMoving = 0;
		return true;
	}
	else if(pDevice_SXPinzaGrueso->IsTutching() && pDevice_SXPinzaFino->IsTutching()) {
		std::cout << "WARNING: X MOVING DESPITE CLOSED CLIPS ON BOTH MOTORS!\n";
		//TODO: turn on error light in simulation
	}
	else if(fast && pDevice_SXPinzaGrueso->IsTutching()) {
		if(backward) {
			xMoving = -2;
			pDevice_AXMotorGrueso->GoPosition(LIMIT_X_MIN-0.1, SPEED_FAST);
			pDevice_AXCarro->GoPosition(LIMIT_X_MIN-0.1, SPEED_FAST);
		}
		else {
			xMoving = 2;
			pDevice_AXMotorGrueso->GoPosition(LIMIT_X_MAX+0.1, SPEED_FAST);
			pDevice_AXCarro->GoPosition(LIMIT_X_MAX+0.1, SPEED_FAST);
		}
		return true;
	}
	else if(!fast && pDevice_SXPinzaFino->IsTutching()) {
		if(backward) {
			xMoving = -1;
			pDevice_AXMotorFino->GoPosition(LIMIT_X_MIN-0.1, SPEED_SLOW);
			pDevice_AXCarro->GoPosition(LIMIT_X_MIN-0.1, SPEED_SLOW);
		}
		else {
			xMoving = 1;
			pDevice_AXMotorFino->GoPosition(LIMIT_X_MAX+0.1, SPEED_SLOW);
			pDevice_AXCarro->GoPosition(LIMIT_X_MAX+0.1, SPEED_SLOW);
		}
		return true;
	}
	return false;
}

bool NumerexControl::moveY(bool fast, bool backward, bool move)
{
	if(!move) {
		pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition());
		pDevice_AYMotorGrueso->GoPosition(pDevice_AYMotorGrueso->GetPosition());
		pDevice_AYMotorFino->GoPosition(pDevice_AYMotorFino->GetPosition());
		yMoving = 0;
		return true;
	}
	else if(pDevice_SYPinzaGrueso->IsTutching() && pDevice_SYPinzaFino->IsTutching()) {
		std::cout << "WARNING: Y MOVING DESPITE CLOSED CLIPS ON BOTH MOTORS!\n";
		//TODO: turn on error light in simulation
	}
	else if(fast && pDevice_SYPinzaGrueso->IsTutching()) {
		if(backward) {
			yMoving = -2;
			pDevice_AYMotorGrueso->GoPosition(LIMIT_Y_MIN-0.1, SPEED_FAST);
			pDevice_AYCarro->GoPosition(LIMIT_Y_MIN-0.1, SPEED_FAST);
		}
		else {
			yMoving = 2;
			pDevice_AYMotorGrueso->GoPosition(LIMIT_Y_MAX+0.1, SPEED_FAST);
			pDevice_AYCarro->GoPosition(LIMIT_Y_MAX+0.1, SPEED_FAST);
		}
		return true;
	}
	else if(!fast && pDevice_SYPinzaFino->IsTutching()) {
		if(backward) {
			yMoving = -1;
			pDevice_AYMotorFino->GoPosition(LIMIT_Y_MIN-0.1, SPEED_SLOW);
			pDevice_AYCarro->GoPosition(LIMIT_Y_MIN-0.1, SPEED_SLOW);
		}
		else {
			yMoving = 1;
			pDevice_AYMotorFino->GoPosition(LIMIT_Y_MAX+0.1, SPEED_SLOW);
			pDevice_AYCarro->GoPosition(LIMIT_Y_MAX+0.1, SPEED_SLOW);
		}
		return true;
	}
	return false;
}

bool NumerexControl::moveZ(bool fast, bool backward, bool move)
{
	if(!move) {
		pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition());
		pDevice_AZMotorGrueso->GoPosition(pDevice_AZMotorGrueso->GetPosition());
		pDevice_AZMotorFino->GoPosition(pDevice_AZMotorFino->GetPosition());
		zMoving = 0;
		return true;
	}
	else if(pDevice_SZPinzaGrueso->IsTutching() && pDevice_SZPinzaFino->IsTutching()) {
		std::cout << "WARNING: Z MOVING DESPITE CLOSED CLIPS ON BOTH MOTORS!\n";
		//TODO: turn on error light in simulation
	}
	else if(fast && pDevice_SZPinzaGrueso->IsTutching()) {
		if(backward) {
			zMoving = -2;
			pDevice_AZMotorGrueso->GoPosition(LIMIT_Z_MIN-0.1, SPEED_FAST);
			pDevice_AZCarro->GoPosition(LIMIT_Z_MIN-0.1, SPEED_FAST);
		}
		else {
			zMoving = 2;
			pDevice_AZMotorGrueso->GoPosition(LIMIT_Z_MAX+0.1, SPEED_FAST);
			pDevice_AZCarro->GoPosition(LIMIT_Z_MAX+0.1, SPEED_FAST);
		}
		return true;
	}
	else if(!fast && pDevice_SZPinzaFino->IsTutching()) {
		if(backward) {
			zMoving = -1;
			pDevice_AZMotorFino->GoPosition(LIMIT_Z_MIN-0.1, SPEED_SLOW);
			pDevice_AZCarro->GoPosition(LIMIT_Z_MIN-0.1, SPEED_SLOW);
		}
		else {
			zMoving = 1;
			pDevice_AZMotorFino->GoPosition(LIMIT_Z_MAX+0.1, SPEED_SLOW);
			pDevice_AZCarro->GoPosition(LIMIT_Z_MAX+0.1, SPEED_SLOW);
		}
		return true;
	}
	return false;
}

void NumerexControl::setXFastConnected(bool position)
{
	//std::cout << "FAST CONNECT? " << position << ", ALREADY? " << pDevice_SXPinzaFino->IsTutching() << "\n";
	if(position == 0 && pDevice_SXPinzaGrueso->IsTutching()) {
		pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition(), SPEED_FAST);
		pDevice_AXPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AXMotorGrueso->GoPosition(X_POS_STANDARD, SPEED_FAST);
	}
	else if(position == 1 && !pDevice_SXPinzaGrueso->IsTutching()) {
		pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition(), SPEED_FAST);
		pDevice_AXMotorFino->GoPosition(pDevice_AXMotorFino->GetPosition(), SPEED_FAST);
		pDevice_AXPinzaGrueso->GoPosition(PINZA_X_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::setXSlowConnected(bool position)
{
	//std::cout << "SLOW CONNECT? " << position << ", ALREADY? " << pDevice_SXPinzaFino->IsTutching() << "\n";
	if(position == 0 && pDevice_SXPinzaFino->IsTutching()) {
		pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition(), SPEED_FAST);
		pDevice_AXPinzaFino->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AXMotorFino->GoPosition(X_POS_STANDARD, SPEED_FAST);
	}
	else if(position == 1 && !pDevice_SXPinzaFino->IsTutching()) {
		pDevice_AXCarro->GoPosition(pDevice_AXCarro->GetPosition(), SPEED_FAST);
		pDevice_AXMotorGrueso->GoPosition(pDevice_AXMotorGrueso->GetPosition(), SPEED_FAST);
		pDevice_AXPinzaFino->GoPosition(PINZA_X_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::setYFastConnected(bool position)
{
	if(position == 0 && pDevice_SYPinzaGrueso->IsTutching()) {
		pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition(), SPEED_FAST);
		pDevice_AYPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AYMotorGrueso->GoPosition(Y_POS_STANDARD, SPEED_FAST);
	}
	else if(position == 1 && !pDevice_SYPinzaGrueso->IsTutching()) {
		pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition(), SPEED_FAST);
		pDevice_AYMotorFino->GoPosition(pDevice_AYMotorFino->GetPosition(), SPEED_FAST);
		pDevice_AYPinzaGrueso->GoPosition(PINZA_Y_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::setYSlowConnected(bool position)
{
	if(position == 0 && pDevice_SYPinzaFino->IsTutching()) {
		pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition(), SPEED_FAST);
		pDevice_AYPinzaFino->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AYMotorFino->GoPosition(Y_POS_STANDARD, SPEED_FAST);
	}
	else if(position == 1 && !pDevice_SYPinzaFino->IsTutching()) {
		pDevice_AYCarro->GoPosition(pDevice_AYCarro->GetPosition(), SPEED_FAST);
		pDevice_AYMotorGrueso->GoPosition(pDevice_AYMotorGrueso->GetPosition(), SPEED_FAST);
		pDevice_AYPinzaFino->GoPosition(PINZA_Y_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::setZFastConnected(bool position)
{
	if(position == 0 && pDevice_SZPinzaGrueso->IsTutching()) {
		pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition(), SPEED_FAST);
		pDevice_AZPinzaGrueso->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AZMotorGrueso->GoPosition(Z_POS_STANDARD, SPEED_FAST);
		if(!pDevice_SZPinzaFino->IsTutching()) {
			pDevice_AZCarro->GoPosition(-1, SPEED_FAST);
			std::cout << "ERROR: AXIS Z HAS FALLEN DOWN BECAUSE BOTH MOTOR CLIPS HAVE BEEN OPENED!\n++++++ PLEASE RESTART SIMULATION! ++++++\n";
			//TODO: TURN ON ERROR LIGHT! SENSING HEAD MIGHT HAVE BEEN DAMAGED
		}
	}
	else if(position == 1 && !pDevice_SZPinzaGrueso->IsTutching()) {
		pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition(), SPEED_FAST);
		pDevice_AZMotorFino->GoPosition(pDevice_AZMotorFino->GetPosition(), SPEED_FAST);
		pDevice_AZPinzaGrueso->GoPosition(PINZA_Z_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::setZSlowConnected(bool position)
{
	if(position == 0 && pDevice_SZPinzaFino->IsTutching()) {
		pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition(), SPEED_FAST);
		pDevice_AZPinzaFino->GoPosition(PINZAS_POS_OPENED, SPEED_FAST);
		pDevice_AZMotorFino->GoPosition(Z_POS_STANDARD, SPEED_FAST);
		if(!pDevice_SZPinzaGrueso->IsTutching()) {
			pDevice_AZCarro->GoPosition(-1, SPEED_FAST);
			//TODO: TURN ON ERROR LIGHT! SENSING HEAD MIGHT HAVE BEEN DAMAGED
		}
	}
	else if(position == 1 && !pDevice_SZPinzaFino->IsTutching()) {
		pDevice_AZCarro->GoPosition(pDevice_AZCarro->GetPosition(), SPEED_FAST);
		pDevice_AZMotorGrueso->GoPosition(pDevice_AZMotorGrueso->GetPosition(), SPEED_FAST);
		pDevice_AZPinzaFino->GoPosition(PINZA_Z_POS_CLOSED, SPEED_FAST);
	}
}

void NumerexControl::moveXToPosition(float position, bool fast)
{
	/* check axis limits */
	if(position < LIMIT_X_MIN || position > LIMIT_X_MAX) return; //TODO: implement check at server for all axis

	/* stop movement of axis */
	NumerexControl::moveX(false, false, false);

	/* start movement towards target */
	if(fast) {
		NumerexControl::setXFastConnected(true);
		NumerexControl::setXSlowConnected(false);
		xMoving = (position < pDevice_AXCarro->GetPosition() ? -2 : 2);
		xTarget = position;
		pDevice_AXCarro->GoPosition(position, SPEED_FAST);
		pDevice_AXMotorGrueso->GoPosition(position, SPEED_FAST);
	}
	else {
		NumerexControl::setXSlowConnected(true);
		NumerexControl::setXFastConnected(false);
		xMoving = (position < pDevice_AXCarro->GetPosition() ? -1 : 1);
		xTarget = NULL;
		pDevice_AXCarro->GoPosition(position, SPEED_SLOW);
		pDevice_AXMotorFino->GoPosition(position, SPEED_SLOW);
	}

	// boundary check function changes to motor slow when axis is close to target position
}

void NumerexControl::moveYToPosition(float position, bool fast)
{
	/* check axis limits */
	if(position < LIMIT_Y_MIN || position > LIMIT_Y_MAX) return;

	/* stop movement of axis */
	NumerexControl::moveY(false, false, false);

	/* start movement towards target */
	if(fast) {
		NumerexControl::setYFastConnected(true);
		NumerexControl::setYSlowConnected(false);
		yMoving = (position < pDevice_AYCarro->GetPosition() ? -2 : 2);
		yTarget = position;
		pDevice_AYCarro->GoPosition(position, SPEED_FAST);
		pDevice_AYMotorGrueso->GoPosition(position, SPEED_FAST);
	}
	else {
		NumerexControl::setYSlowConnected(true);
		NumerexControl::setYFastConnected(false);
		yMoving = (position < pDevice_AYCarro->GetPosition() ? -1 : 1);
		yTarget = NULL;
		pDevice_AYCarro->GoPosition(position, SPEED_SLOW);
		pDevice_AYMotorFino->GoPosition(position, SPEED_SLOW);
	}

	// boundary check function changes to motor slow when axis is close to target position
}

void NumerexControl::moveZToPosition(float position, bool fast)
{
	/* check axis limits */
	if(position < LIMIT_Z_MIN || position > LIMIT_Z_MAX) return;

	/* stop movement of axis */
	NumerexControl::moveZ(false, false, false);

	/* start movement towards target */
	if(fast) {
		NumerexControl::setZFastConnected(true);
		NumerexControl::setZSlowConnected(false);
		zMoving = (position < pDevice_AZCarro->GetPosition() ? -2 : 2);
		zTarget = position;
		pDevice_AZCarro->GoPosition(position, SPEED_FAST);
		pDevice_AZMotorGrueso->GoPosition(position, SPEED_FAST);
	}
	else {
		NumerexControl::setZSlowConnected(true);
		NumerexControl::setZFastConnected(false);
		zMoving = (position < pDevice_AZCarro->GetPosition() ? -1 : 1);
		zTarget = NULL;
		pDevice_AZCarro->GoPosition(position, SPEED_SLOW);
		pDevice_AZMotorFino->GoPosition(position, SPEED_SLOW);
	}

	// boundary check function changes to motor slow when axis is close to target position
}
