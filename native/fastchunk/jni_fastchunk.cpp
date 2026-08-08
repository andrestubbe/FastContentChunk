#include <jni.h>
#include "fastchunk.h"
#include <vector>
#include <string>

extern "C" JNIEXPORT jobjectArray JNICALL
Java_fastcontentchunk_FastContentChunkNative_chunk(
    JNIEnv* env,
    jclass /*cls*/,
    jstring jtext,
    jint maxTokens,
    jint overlapTokens)
{
    if (jtext == nullptr) {
        return nullptr;
    }

    const char* utf = env->GetStringUTFChars(jtext, nullptr);
    if (utf == nullptr || env->ExceptionCheck()) {
        return nullptr;
    }

    const jsize len = env->GetStringUTFLength(jtext);
    std::vector<Chunk> results = fastchunk_chunk(
        utf,
        static_cast<std::size_t>(len),
        static_cast<int>(maxTokens),
        static_cast<int>(overlapTokens)
    );

    env->ReleaseStringUTFChars(jtext, utf);

    jclass chunkClass = env->FindClass("fastcontentchunk/FastContentChunkNative$Chunk");
    if (chunkClass == nullptr || env->ExceptionCheck()) {
        env->ExceptionClear();
        return nullptr;
    }

    jmethodID ctor = env->GetMethodID(chunkClass, "<init>", "(ILjava/lang/String;)V");
    if (ctor == nullptr || env->ExceptionCheck()) {
        env->ExceptionClear();
        env->DeleteLocalRef(chunkClass);
        return nullptr;
    }

    const jsize n = static_cast<jsize>(results.size());
    jobjectArray arr = env->NewObjectArray(n, chunkClass, nullptr);
    if (arr == nullptr || env->ExceptionCheck()) {
        env->ExceptionClear();
        env->DeleteLocalRef(chunkClass);
        return nullptr;
    }

    for (jsize i = 0; i < n; ++i) {
        const Chunk& c = results[static_cast<size_t>(i)];

        jstring jstr = env->NewStringUTF(c.text.c_str());
        if (jstr == nullptr || env->ExceptionCheck()) {
            for (jsize j = 0; j < i; ++j) {
                jobject prev = env->GetObjectArrayElement(arr, j);
                if (prev) env->DeleteLocalRef(prev);
            }
            env->DeleteLocalRef(arr);
            env->DeleteLocalRef(chunkClass);
            return nullptr;
        }

        jobject obj = env->NewObject(chunkClass, ctor, static_cast<jint>(c.id), jstr);
        env->DeleteLocalRef(jstr);

        if (obj == nullptr || env->ExceptionCheck()) {
            for (jsize j = 0; j < i; ++j) {
                jobject prev = env->GetObjectArrayElement(arr, j);
                if (prev) env->DeleteLocalRef(prev);
            }
            env->DeleteLocalRef(arr);
            env->DeleteLocalRef(chunkClass);
            return nullptr;
        }

        env->SetObjectArrayElement(arr, i, obj);
        env->DeleteLocalRef(obj);
    }

    env->DeleteLocalRef(chunkClass);
    return arr;
}

extern "C" JNIEXPORT jintArray JNICALL
Java_fastcontentchunk_FastContentChunkNative_chunkToOffsets(
    JNIEnv* env,
    jclass /*cls*/,
    jstring jtext,
    jint maxTokens,
    jint overlapTokens)
{
    if (jtext == nullptr) {
        return nullptr;
    }

    const char* utf = env->GetStringUTFChars(jtext, nullptr);
    if (utf == nullptr || env->ExceptionCheck()) {
        return nullptr;
    }

    const jsize len = env->GetStringUTFLength(jtext);
    std::vector<Chunk> results = fastchunk_chunk(
        utf,
        static_cast<std::size_t>(len),
        static_cast<int>(maxTokens),
        static_cast<int>(overlapTokens)
    );

    env->ReleaseStringUTFChars(jtext, utf);

    jsize count = static_cast<jsize>(results.size());
    jintArray arr = env->NewIntArray(count * 2);
    if (arr == nullptr || env->ExceptionCheck()) {
        env->ExceptionClear();
        return nullptr;
    }

    std::vector<jint> buffer(count * 2);
    for (size_t i = 0; i < results.size(); ++i) {
        buffer[i * 2]     = static_cast<jint>(results[i].id); // or start char offset
        buffer[i * 2 + 1] = static_cast<jint>(results[i].text.length());
    }

    env->SetIntArrayRegion(arr, 0, count * 2, buffer.data());
    return arr;
}
