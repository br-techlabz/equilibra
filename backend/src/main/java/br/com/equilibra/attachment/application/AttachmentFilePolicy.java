package br.com.equilibra.attachment.application;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

public class AttachmentFilePolicy {
    private final AttachmentProperties properties;
    public AttachmentFilePolicy(AttachmentProperties properties){this.properties=properties;}
    public String validateAndDetect(InputStream input,String declared,long size)throws IOException{
        if(size<=0||size>properties.getMaxFileSize())throw new IllegalArgumentException("Invalid attachment size");
        if(!properties.getAllowedMediaTypes().contains(declared))throw new IllegalArgumentException("Unsupported attachment media type");
        byte[] probe=input.readNBytes(16); boolean valid=switch(declared){case "application/pdf" -> starts(probe,new byte[]{0x25,0x50,0x44,0x46});case "image/png" -> starts(probe,new byte[]{(byte)0x89,0x50,0x4e,0x47});case "image/jpeg" -> starts(probe,new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff});case "image/webp" -> starts(probe,new byte[]{0x52,0x49,0x46,0x46})&&probe.length>=12&&probe[8]=='W'&&probe[9]=='E'&&probe[10]=='B'&&probe[11]=='P';default -> false;};if(!valid)throw new IllegalArgumentException("Attachment signature does not match media type");return declared;}
    public static String checksum(InputStream in)throws IOException{try{MessageDigest digest=MessageDigest.getInstance("SHA-256");in.transferTo(new java.io.OutputStream(){public void write(int b){digest.update((byte)b);}public void write(byte[] b,int o,int l){digest.update(b,o,l);}});return HexFormat.of().formatHex(digest.digest());}catch(Exception e){throw new IOException("Unable to checksum attachment",e);}}
    private static boolean starts(byte[] value,byte[] prefix){if(value.length<prefix.length)return false;for(int i=0;i<prefix.length;i++)if(value[i]!=prefix[i])return false;return true;}
}
