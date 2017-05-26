using System;
using NUnit.Framework;
using PeterO.Cbor;

namespace PeterO.Cbor.Tests {
  [TestFixture]
  public class CBORExceptionTest {
    [Test]
    public void TestConstructor() {
      Assert.Throws<CBORException>(() => throw new CBORException("Test exception"));
    }
  }
}
