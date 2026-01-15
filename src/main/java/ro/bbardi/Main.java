package ro.bbardi;

import org.bouncycastle.asn1.*;

import javax.smartcardio.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static HexFormat hexFormat = HexFormat.of();

    public static String readPIN(){
        System.out.println("Introduce-ti PINul: ");
        Scanner scanner = new Scanner(System.in);
        try {
            return scanner.next("\\d{4}");
        } catch (Exception e) {
            System.out.println("PINul trebuie sa fie 4 cifre!\n");
            return "";
        }
    }
    public static void transmitInitAPDU(CardChannel c) throws CardException {
        ResponseAPDU resp = c.transmit(new CommandAPDU(hexFormat.parseHex("00A4040010A000000077030C60000000FE0000050000")));
        if(resp.getSW() != 0x9000){throw new CardException("Init APDU failed");}
    }

    public static boolean authenticate(CardChannel c, String pin) throws CardException {
        String pinAsHex = hexFormat.formatHex(pin.getBytes());
        ResponseAPDU resp = c.transmit(new CommandAPDU(hexFormat.parseHex("002000030c"+pinAsHex+"ffffffffffffffff")));
        if(resp.getSW() == 0x9000){
            return true; // PIN corect
        }
        if(((resp.getSW() & 0xFFF0) == 0x63C0)){
            System.out.printf("PIN-ul este incorect. Mai aveti %d incercari \n", (resp.getSW() & 0x000F));
            return false;
        }
        System.out.printf("PIN-ul este blocat.\n");
        return false;

    }
    public static void selectPersonalFile(CardChannel c) throws CardException{
        ResponseAPDU resp = c.transmit(new CommandAPDU(hexFormat.parseHex("00A4040C0FE828bd080fa0000001674544415441")));
        if(resp.getSW() != 0x9000){throw new CardException("Select APDU failed");}
    }

    public static byte[] readData(CardChannel c, String dataSelectAPDU) throws CardException {
        ResponseAPDU resp = c.transmit(new CommandAPDU(hexFormat.parseHex(dataSelectAPDU)));
        if(resp.getSW() != 0x9000){throw new CardException("Select file APDU failed");}
        resp = c.transmit(new CommandAPDU(hexFormat.parseHex("00B0000000")));
        if(resp.getSW() != 0x9000){throw new CardException("Select file APDU failed");}
        return resp.getData();
    }

    public static void getPersonalInfo(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020101");
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        System.out.format("Nume: %s\n", list.get(0));
        System.out.format("Prenume: %s\n", list.get(1));
        System.out.format("Sex: %s\n", list.get(2));
        System.out.format("Data Nasterii: %s\n", list.get(3));
        System.out.format("CNP: %s\n", list.get(4));
        System.out.format("Cetatenie: %s\n", list.get(5));
    }

    public static void getBirthData(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020102");
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        //System.out.format("Data nasterii: %s\n", list.get(0)); Avem deja data la personalInfo
        System.out.format("Locul nasterii: %s\n", list.get(1));
    }

    public static void getIssueInfo(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020104");
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        System.out.format("Nr Act: %s\n", list.get(0));
        System.out.format("Data emiterii: %s\n", list.get(1));
        System.out.format("Data expirarii: %s\n", list.get(2));
        System.out.format("Autoritatea emitenta: %s\n", list.get(3));
    }

    public static void getAddressInfo(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020106");
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        System.out.format("Domiciliu: %s\n", list.get(0));
    }

    public static void getSecondaryAddressInfo(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020107");
        if(data.length == 0 || data[0] == '0'){ return; } //n-avem flotant
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        System.out.format("Flotant: %s\n", list.get(0));
        System.out.format("Data de Inceput: %s\n", list.get(1));
        System.out.format("Data de Expirare: %s\n", list.get(2));
    }

    public static void getForeignAddressInfo(CardChannel c) throws CardException {
        byte[] data = readData(c,"00A4020C020108");
        if(data.length == 0 || data[0] == '0'){ return;} //n-avem domiciliu strainezia
        ASN1InputStream asn1InputStream = new ASN1InputStream(new ByteArrayInputStream(data));
        List<String> list = new ArrayList<>();
        try {
            ASN1Sequence asn1Sequence = (ASN1Sequence) asn1InputStream.readObject();
            for (ASN1Encodable x : asn1Sequence) {
                ASN1OctetString octetString = (ASN1OctetString) ((ASN1TaggedObject) x).getBaseObject().toASN1Primitive();
                list.add(new String(octetString.getOctets(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
        }
        System.out.format("Domiciliu in strainatate: %s\n", list.get(0));
        System.out.format("Data de Inceput: %s\n", list.get(1)); //Are aceleasi campuri ca si viza de flotant
        System.out.format("Data de Expirare: %s\n", list.get(2)); //Are aceleasi campuri ca si viza de flotant
    }

    public static void main(String[] args) throws CardException, IOException {

        String pin = "";
        while (pin.isEmpty()) {
            pin = readPIN();
        }
        for (CardTerminal terminal : TerminalFactory.getDefault().terminals().list()) {
            if (!terminal.isCardPresent()) {
                continue;
            }
            CardChannel c = terminal.connect("*").getBasicChannel();
            if (!Arrays.equals(c.getCard().getATR().getBytes(), hexFormat.parseHex("3bdf96008131fe4580738421e05569780000808307900024"))) {
                continue;
            }
            transmitInitAPDU(c);
            if (!authenticate(c, pin)) {
                continue;
            }
            selectPersonalFile(c);
            getPersonalInfo(c);
            getBirthData(c);
            getIssueInfo(c);
            getAddressInfo(c);
            getSecondaryAddressInfo(c);
            getForeignAddressInfo(c);
        }
    }
}