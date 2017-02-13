package mmcontrol.machineconnection;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcontrol.machineconnection.impl.tcpsocket.TCPMachineConnectionService;
import mmcontrol.machineconnection.interfaces.IMachineConnectionService;

/**
 *
 * @author Michael Zawrel
 */
public class MMConnectionService {

    public static void main(String[] args) {

        try {
            IMachineConnectionService server = new TCPMachineConnectionService();

            new Thread((TCPMachineConnectionService) server).start();

            System.out.println("ConnectionService running... type any key to exit!");

            try {
                System.in.read();
            } catch (IOException ex) {
                Logger.getLogger(MMConnectionService.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println("Exiting...");

            server.shutdown();
        } catch (RemoteException ex) {
            System.out.println("Unable to start!");
        }

        /**
         * language selection - good solution? if(args.length == 1) {
         * java.io.InputStream is_lang = null; if(args[0].equals("es")) is_lang
         * =
         * ClassLoader.getSystemResourceAsStream("../common/lang_es.properties");
         * else if(args[0].equals("en")) is_lang =
         * ClassLoader.getSystemResourceAsStream("../common/lang_en.properties");
         * else if(args[0].equals("de")) is_lang =
         * ClassLoader.getSystemResourceAsStream("../common/lang_de.properties");
         * if (is_lang != null) { java.util.Properties props_lang = new
         * java.util.Properties(); props_lang.load(is_lang); } }
         */
    }

}
