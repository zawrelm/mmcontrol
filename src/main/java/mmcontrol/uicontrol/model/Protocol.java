package mmcontrol.uicontrol.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO: add persistence annotations
public class Protocol implements Serializable {
    
        public List<String> protocol;
        
        public Protocol() {
            this.protocol = new ArrayList<>();
        }
        
        public void addLine(String line) {
            this.protocol.add(this.protocol.size() + ": " + line);
        }
        
        public void appendToLine(int lineNumber, String appendix) {
            if(lineNumber > 0 && lineNumber <= this.protocol.size())
                this.protocol.set(lineNumber, this.protocol.get(lineNumber) +appendix);
        }
        
        public String getLine(int lineNumber) {
            if(lineNumber > 0 && lineNumber <= this.protocol.size())
                return this.protocol.get(lineNumber-1);
            return "ERROR: INVALID LINE!";
        }
        
        public int getLength() {
            return this.protocol.size();
        }

        @Override
        public String toString() {
            String text = "";
            Iterator<String> it = this.protocol.iterator();
            while(it.hasNext()) {
                text += it.next() +"\n";
            }
            
            return text;
        }
        
}
