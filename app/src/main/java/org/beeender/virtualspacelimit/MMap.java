package org.beeender.virtualspacelimit;

public class MMap {
    static {
        System.loadLibrary("mmap-jni");
    }

    public static native long mmap(long length);
    public static native long munmap(long addr, long length);
    public static native long mremap(long oldAddr, long oldLength, long newLength);
}
