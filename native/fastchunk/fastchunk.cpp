/* Accelerated by FastSIMD AVX2 Hardware Engine */
#include "fastchunk.h"
#include <immintrin.h>
#include <cstring>
#include <vector>
#include <algorithm>

static void tokenize_indices(
    const char* text,
    std::size_t len,
    std::vector<int>& tokenStarts,
    std::vector<int>& tokenEnds
) {
    tokenStarts.clear();
    tokenEnds.clear();
    tokenStarts.reserve(len / 4 + 16);
    tokenEnds.reserve(len / 4 + 16);

    bool inToken = false;
    int currentStart = 0;

    std::size_t i = 0;
    constexpr std::size_t step = 32; // AVX2 32-byte SIMD step

    while (i + step <= len) {
        __m256i chunk = _mm256_loadu_si256(reinterpret_cast<const __m256i*>(text + i));

        __m256i spaces = _mm256_set1_epi8(' ');
        __m256i tabs   = _mm256_set1_epi8('\t');
        __m256i nl     = _mm256_set1_epi8('\n');
        __m256i cr     = _mm256_set1_epi8('\r');

        __m256i eqSpace = _mm256_cmpeq_epi8(chunk, spaces);
        __m256i eqTab   = _mm256_cmpeq_epi8(chunk, tabs);
        __m256i eqNl    = _mm256_cmpeq_epi8(chunk, nl);
        __m256i eqCr    = _mm256_cmpeq_epi8(chunk, cr);

        __m256i wsMask = _mm256_or_si256(
            _mm256_or_si256(eqSpace, eqTab),
            _mm256_or_si256(eqNl, eqCr)
        );

        unsigned int mask = static_cast<unsigned int>(_mm256_movemask_epi8(wsMask));

        for (int b = 0; b < 32; ++b) {
            bool isWs = (mask & (1u << b)) != 0;
            int pos = static_cast<int>(i + b);

            if (!isWs) {
                if (!inToken) {
                    inToken = true;
                    currentStart = pos;
                }
            } else {
                if (inToken) {
                    inToken = false;
                    tokenStarts.push_back(currentStart);
                    tokenEnds.push_back(pos);
                }
            }
        }
        i += step;
    }

    // Remainder scalar pass for len < 32 or remaining trailing bytes
    while (i < len) {
        char c = text[i];
        bool isWs = (c == ' ' || c == '\t' || c == '\n' || c == '\r');
        int pos = static_cast<int>(i);

        if (!isWs) {
            if (!inToken) {
                inToken = true;
                currentStart = pos;
            }
        } else {
            if (inToken) {
                inToken = false;
                tokenStarts.push_back(currentStart);
                tokenEnds.push_back(pos);
            }
        }
        ++i;
    }

    if (inToken) {
        tokenStarts.push_back(currentStart);
        tokenEnds.push_back(static_cast<int>(len));
    }
}

std::vector<Chunk> fastchunk_chunk(
    const char* utf8Text,
    std::size_t len,
    int maxTokens,
    int overlapTokens
) {
    if (utf8Text == nullptr || len == 0 || maxTokens <= 0) {
        return {};
    }
    if (overlapTokens < 0) overlapTokens = 0;
    if (overlapTokens >= maxTokens) overlapTokens = maxTokens - 1;

    std::vector<int> starts;
    std::vector<int> ends;
    tokenize_indices(utf8Text, len, starts, ends);

    const int tokenCount = static_cast<int>(starts.size());
    if (tokenCount == 0) return {};

    std::vector<Chunk> chunks;
    chunks.reserve(static_cast<size_t>(std::max(1, tokenCount / maxTokens + 1)));

    int id = 0;
    int startIdx = 0;

    while (startIdx < tokenCount) {
        int endIdx = std::min(startIdx + maxTokens, tokenCount);

        int byteStart = starts[startIdx];
        int byteEnd   = ends[endIdx - 1];

        Chunk c;
        c.id = id++;
        c.text.assign(utf8Text + byteStart, static_cast<size_t>(byteEnd - byteStart));
        chunks.push_back(std::move(c));

        if (endIdx == tokenCount) break;
        startIdx = std::max(0, endIdx - overlapTokens);
    }

    return chunks;
}
