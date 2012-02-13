package com.ettrema.binary;

import com.bradmcevoy.http.Resource;
import com.ettrema.event.*;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.NameNode;
import com.ettrema.web.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Checksum;
import org.apache.commons.io.output.NullOutputStream;

/**
 *
 * @author brad
 */
public class StateTokenManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StateTokenManager.class);
    public static final String NAMENODE_CRC = "_sys_state-token";

    public StateTokenManager(EventManager eventManager) {
        StateTokenManagerEventLister eventLister = new StateTokenManagerEventLister();
        eventManager.registerEventListener(eventLister, PreSaveEvent.class);
        eventManager.registerEventListener(eventLister, ClydeMoveEvent.class);
        eventManager.registerEventListener(eventLister, PhysicalDeleteEvent.class);
    }

    public Long getStateToken(Folder f) {
        StateToken stateToken = getOrCreateStateToken(f, false);
        if (stateToken != null) {
            return stateToken.getCrc();
        } else {
            return null;
        }
    }

    private void setStateToken(Folder f, long token) {
        StateToken stateToken = getOrCreateStateToken(f, true);
        stateToken.setCrc(token);
    }

    private void removeStateToken(Folder f) {
        StateToken stateToken = getOrCreateStateToken(f, false);
        if (stateToken != null) {
            stateToken.delete();
        }
    }

    /**
     * Urghh, syncronised...
     * 
     * @param f
     * @param autoCreate
     * @return 
     */
    private synchronized StateToken getOrCreateStateToken(Folder f, boolean autoCreate) {
        NameNode nn = f.getNameNode().child(NAMENODE_CRC);
        StateToken stateToken;
        if (nn == null) {
            if (autoCreate) {
                stateToken = new StateToken();
                f.getNameNode().add(NAMENODE_CRC, stateToken);
                return stateToken;
            } else {
                return null;
            }
        } else {
            StateToken token = (StateToken) nn.getData();
            if( token == null ) {
                nn.delete();
                stateToken = new StateToken();
                f.getNameNode().add(NAMENODE_CRC, stateToken);
                return stateToken;
            }
            return token;
        }
    }

    public String getStateTokenData(Folder f) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        long crc = _calcBinaryCrc(f, bout);
        return bout.toString() + " ==> " + crc;
    }

    /**
     * recalculate the binary CRC value for this folder by hashing the crc's of
     * the folders and binary files contained within it
     *
     */
    public void calcBinaryCrc(Folder f) {
        NullOutputStream nullOut = new NullOutputStream();
        long crc = _calcBinaryCrc(f, nullOut);
        setStateToken(f, crc);
    }

    private long _calcBinaryCrc(Folder f, OutputStream nullOut) {
        LogUtils.trace(log, "calcBinaryCrc", f.getName());
        CheckedOutputStream cout = new CheckedOutputStream(nullOut, new Adler32());
        for (Resource r : f.getChildren()) {
            String line;
            if (r instanceof Folder) {
                Folder child = (Folder) r;
                if (!child.isSystemFolder()) {
                    if (child.getBinaryStateToken() == null) {
                        calcBinaryCrc(child);
                    }
                    line = toHashableText(child.getName(), child.getBinaryStateToken());
                } else {
                    line = null;
                }
            } else if (r instanceof LinkedFolder) {
                LinkedFolder lf = (LinkedFolder) r;
                Folder linkedTo = lf.getLinkedTo();
                if (linkedTo != null) {
                    if (linkedTo.getBinaryStateToken() == null) {
                        calcBinaryCrc(linkedTo);
                    }
                    line = toHashableText(lf.getName(), linkedTo.getBinaryStateToken());
                } else {
                    line = null;
                    log.warn("Can't calculate crc on linked folder: " + lf.getHref() + " Can't resolve link");
                }
            } else if (r instanceof BinaryFile) {
                BinaryFile bf = (BinaryFile) r;
                line = toHashableText(bf.getName(), bf.getCrc());
            } else {
                line = null;
            }
            appendLine(line, cout);
        }
        Checksum check = cout.getChecksum();
        long crc = check.getValue();
        return crc;
    }

    private String toHashableText(String name, Long crc) {
        String line = name + ":" + crc + '\n';
        return line;
    }

    private void appendLine(String line, CheckedOutputStream cout) {
        if (line == null) {
            return;
        }
        try {
            cout.write(line.getBytes());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Called when we know that the folder's binaryCrc value is no longer valid
     *
     * Builds a list of "top" folders as it goes. Those are the folders which
     * act as state sync roots, such as User and Host
     *
     * @param f
     * @return
     */
    private void flushBinaryCrc(Folder f, List<Folder> tops) {
        removeStateToken(f);
        if (f.hasLinkedFolders()) {
            for (LinkedFolder lf : f.getLinkedFolders()) {
                flushBinaryCrc(lf.getParent(), tops);
            }
        }
        if (f instanceof Host || f instanceof User) {
            tops.add(f);
            return; // thats as far as we should go
        }
        Folder parent = f.getParent();
        if (parent == null) {
            tops.add(f);
            return;
        }
        flushBinaryCrc(parent, tops);
    }

    private class StateTokenManagerEventLister implements EventListener {

        @Override
        public void onEvent(Event e) {
            LogUtils.trace(log, "onEvent", e.getClass());
            List<Folder> tops = new ArrayList<Folder>();
            if (e instanceof PreSaveEvent) {
                PreSaveEvent pse = (PreSaveEvent) e;
                if (pse.getResource() instanceof BinaryFile) {
                    BinaryFile bf = (BinaryFile) pse.getResource();
                    flushBinaryCrc(bf.getParent(), tops);
                } else if (pse.getResource() instanceof Folder) {
                    Folder f = (Folder) pse.getResource();
                    flushBinaryCrc(f, tops);
                } else if (pse.getResource() instanceof LinkedFolder) {
                    LinkedFolder lf = (LinkedFolder) pse.getResource();
                    flushBinaryCrc(lf.getParent(), tops);
                }
            } else if (e instanceof ClydeMoveEvent) {
                // Updating CRC on the new folder will be taken care of by saving the actual resource
                // But need to update the old folder too
                ClydeMoveEvent cme = (ClydeMoveEvent) e;
                Folder oldParent = cme.getOldParent();
                flushBinaryCrc(oldParent, tops);
            } else if (e instanceof PhysicalDeleteEvent) {
                PhysicalDeleteEvent pde = (PhysicalDeleteEvent) e;
                flushBinaryCrc(pde.getResource().getParent(), tops);
            }
            // If we've found at least one root resource then recalc
            if (!tops.isEmpty()) {
                for (Folder top : tops) {
                    calcBinaryCrc(top);
                }
            }
        }
    }
}
