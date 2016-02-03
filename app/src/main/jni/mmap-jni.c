#include <jni.h>
#include <sys/syscall.h>
#include <sys/mman.h>

JNIEXPORT jlong JNICALL
Java_org_beeender_virtualspacelimit_MMap_mmap(JNIEnv *env, jclass type, jlong length) {
    void* addr = mmap(0, length, PROT_READ | PROT_WRITE, MAP_ANON | MAP_PRIVATE, -1, 0);

    return addr;
}

JNIEXPORT jlong JNICALL
Java_org_beeender_virtualspacelimit_MMap_munmap(JNIEnv *env, jclass type, jlong addr,
                                                jlong length) {
    return munmap(addr, length);
}

JNIEXPORT jlong JNICALL
Java_org_beeender_virtualspacelimit_MMap_mremap(JNIEnv *env, jclass type, jlong oldAddr,
                                                jlong oldLength, jlong newLength) {
    void* addr = mremap(oldAddr, oldLength, newLength, MREMAP_MAYMOVE);

    return addr;
}