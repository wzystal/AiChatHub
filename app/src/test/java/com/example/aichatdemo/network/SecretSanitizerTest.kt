package com.example.aichatdemo.network

import org.junit.Assert.assertEquals
import org.junit.Test

class SecretSanitizerTest {
  @Test
  fun sanitizeAsHttpCredential_stripsControlCharacters() {
    val dirty = "\u001bsk-test-key\n"
    assertEquals("sk-test-key", dirty.sanitizeAsHttpCredential())
  }
}
