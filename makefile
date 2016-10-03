JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	BankInfo.java \
	Peer1.java \
	Peer2.java \
	Peer3.java \
	Peer4.java \
	Client.java \
	CloseUtil.java \
	Send.java \
	Receive.java \
	 
        

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class