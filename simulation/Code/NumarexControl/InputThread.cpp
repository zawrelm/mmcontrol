#include "InputThread.h"
#include "NumerexControl.h"
#include "ServerConnection.h"

ServerConnection * connection;
NumerexControl * control;
int iResult;

InputThread::InputThread(NumerexControl * ctrl)
{

	connection = new ServerConnection();
	control = ctrl;

}

/**
 * Receives user input string via TCP-socket connection and executes it
 */
void InputThread::receiveInstruction()	//TODO: multiple incoming instructions have to be queued
{
	const int MAX_SIZE = 64;
	char buffer[MAX_SIZE]; // buffer to store received message - can only hold one instruction
	iResult = connection->receiveMessage(buffer, (int) strlen(buffer));

	if ( iResult > 0 ) {
		//printf("COMMAND RECEIVED!");
		std::string keyword(buffer, 0, 3);
		
		if(keyword.compare("SET") == 0 && control->getContact() != 1) { //command: SET

			if(buffer[5] == ' ') {
				char actuator[2];
				memcpy(actuator, &buffer[4], 1);
				actuator[1] = '\0';
				char value = buffer[6];

				/* CONTROL ACTUATORS! */
				if(strcmp(actuator, "0") == 0) control->moveX(1, 0, value == '1'? true : false); //params for motors: fast, backward, move
				else if(strcmp(actuator, "1") == 0) control->moveX(1, 1, value == '1'? true : false);
				else if(strcmp(actuator, "2") == 0) control->moveX(0, 0, value == '1'? true : false);
				else if(strcmp(actuator, "3") == 0) control->moveX(0, 1, value == '1'? true : false);

				else if(strcmp(actuator, "4") == 0) control->moveY(1, 0, value == '1'? true : false);
				else if(strcmp(actuator, "5") == 0) control->moveY(1, 1, value == '1'? true : false);
				else if(strcmp(actuator, "6") == 0) control->moveY(0, 0, value == '1'? true : false);
				else if(strcmp(actuator, "7") == 0) control->moveY(0, 1, value == '1'? true : false);

				else if(strcmp(actuator, "8") == 0) control->moveZ(1, 0, value == '1'? true : false);
				else if(strcmp(actuator, "9") == 0) control->moveZ(1, 1, value == '1'? true : false);

				printf("Actuator %s set to %c!\n", actuator, value);
			}
			else {
				char actuator[3];
				memcpy(actuator, &buffer[4], 2);
				actuator[2] = '\0';
				char value = buffer[7];

				/* CONTROL ACTUATORS! */
				if(strcmp(actuator, "10") == 0) control->moveZ(0, 0, value == '1'? true : false);
				else if(strcmp(actuator, "11") == 0) control->moveZ(0, 1, value == '1'? true : false);

				else if(strcmp(actuator, "12") == 0) control->setXFastConnected(value == '1'? true : false);
				else if(strcmp(actuator, "13") == 0) control->setXSlowConnected(value == '1'? true : false);
				else if(strcmp(actuator, "14") == 0) control->setYFastConnected(value == '1'? true : false);
				else if(strcmp(actuator, "15") == 0) control->setYSlowConnected(value == '1'? true : false);
				else if(strcmp(actuator, "16") == 0) control->setZFastConnected(value == '1'? true : false);
				else if(strcmp(actuator, "17") == 0) control->setZSlowConnected(value == '1'? true : false);

				printf("Actuator %s set to %c!\n", actuator, value);
			}
		}
		else if(keyword.compare("POS") == 0 && control->getContact() != 1) { //command: POS
			char axis[2];
			memcpy(axis, &buffer[4], 1);
			axis[1] = '\0';
			//char value[MAX_SIZE];
			std::copy_backward(buffer + 6, buffer + MAX_SIZE, buffer + MAX_SIZE - 6);
			//memcpy(value, &buffer[6], strlen(buffer)-5);
			//value[strlen(buffer)] = '\0';

			std::string vstring(buffer);
			float position = std::stof(vstring);

			if(strcmp(axis, "X") == 0) {
				control->moveXToPosition(position, true);
				printf("Move X to %f\n", position);
			}
			else if(strcmp(axis, "Y") == 0) {
				control->moveYToPosition(position, true);
				printf("Move Y to %f\n", position);
			}
			else if(strcmp(axis, "Z") == 0) {
				control->moveZToPosition(position, true);
				printf("Move Z to %f\n", position);
			}

		}
		else if (keyword.compare("BYE") == 0) { //command: BYE
			printf("Confirming and shutting down...\n");
			control->stopInputThread();
			control->haltMachine(true);
			std::string s = control->getMachineState();

			char *data = new char[s.length() + 4];
			strcpy(data, "BY ");
			strcat(data, s.c_str());
			printf(data);
			
			connection->sendMessage(data, (int) strlen(data));
			delete [] data;
		}
		else { //command: WHO or undefined command
			printf("COMMAND RECEIVED: unexpected command -> No action!\n");
		}
	}
    else if ( iResult == 0 ) {
		printf("Connection closed unexpectedly. Shutting down.\n");
		control->haltMachine(true);
		exit(1);
	}
    else {
		printf("Result: %i\n", iResult);
        printf("recv failed with error: %d\n", WSAGetLastError());
		control->haltMachine(true);
		printf("Immediate shutdown due to error!\n");
		exit(1);
	}

	//cout << GetRandom(5, 9);
	//if(!pDevice_SZPinzaGrueso->IsTutching() && !pDevice_SZPinzaFino->IsTutching()) cout << "Possible damage detected: Both clamps of axis Z opened!";
}