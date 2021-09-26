package cx.turam.log;

import java.io.File;

public interface UnHandlerExListener {
        void handleEx(String pProfile, File pFile, String pS);
    }