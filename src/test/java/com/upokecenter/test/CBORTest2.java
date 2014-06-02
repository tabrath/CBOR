package com.upokecenter.test;
/*
Written by Peter O. in 2014.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/
 */

import java.io.*;
import org.junit.Assert;
import org.junit.Test;
import com.upokecenter.util.*;
import com.upokecenter.cbor.*;

  public class CBORTest2
  {
    @Test
    public void TestCyclicRefs() {
      CBORObject cbor = CBORObject.NewArray();
      cbor.Add(CBORObject.NewArray());
      cbor.Add(cbor);
      cbor.get(0).Add(cbor);
      try {
        cbor.WriteTo(new java.io.ByteArrayOutputStream());
        Assert.fail("Should have failed");
      } catch (IllegalArgumentException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
    }

    @Test
    public void TestEquivalentInfinities() {
        CBORObject co, co2;
        co = CBORObject.FromObject(ExtendedDecimal.PositiveInfinity);
        co2 = CBORObject.FromObject(Double.POSITIVE_INFINITY);
        Assert.assertEquals(0, co.compareTo(co2));
        Assert.assertEquals(0, co2.compareTo(co));
        co = CBORObject.NewMap().Add(ExtendedDecimal.PositiveInfinity, CBORObject.Undefined);
        co2 = CBORObject.NewMap().Add(Double.POSITIVE_INFINITY, CBORObject.Undefined);
        Assert.assertEquals(0, co.compareTo(co2));
        Assert.assertEquals(0, co2.compareTo(co));
    }

    @Test
    public void TestSharedRefs() {
      byte[] bytes;
      CBORObject cbor;
      String expected;
      bytes = new byte[] {  (byte)0x9f, (byte)0xd8, 28, 1, (byte)0xd8, 29, 0, 3, 3, (byte)0xd8, 29, 0, (byte)0xff  };
      cbor = CBORObject.DecodeFromBytes(bytes);
      expected = "[1,1,3,3,1]";
      Assert.assertEquals(expected, cbor.ToJSONString());
      bytes = new byte[] {  (byte)0x9f, (byte)0xd8, 28, (byte)0x81, 1, (byte)0xd8, 29, 0, 3, 3, (byte)0xd8, 29, 0, (byte)0xff  };
      cbor = CBORObject.DecodeFromBytes(bytes);
      expected = "[[1],[1],3,3,[1]]";
      Assert.assertEquals(expected, cbor.ToJSONString());
      // Checks if both objects are the same reference, not just equal
      if(!(cbor.get(0) == cbor.get(1)))Assert.fail( "cbor.get(0) not same as cbor.get(1)");
      if(!(cbor.get(0) == cbor.get(4)))Assert.fail( "cbor.get(0) not same as cbor.get(4)");
      bytes = new byte[] {  (byte)0xd8, 28, (byte)0x82, 1, (byte)0xd8, 29, 0  };
      cbor = CBORObject.DecodeFromBytes(bytes);
      Assert.assertEquals(2, cbor.size());
      // Checks if both objects are the same reference, not just equal
      if(!(cbor == cbor.get(1)))Assert.fail( "objects not the same");
    }

    @Test
    public void TestRationalCompareDecimal() {
      FastRandom fr = new FastRandom();
      // System.Diagnostics.Stopwatch sw1 = new System.Diagnostics.Stopwatch();
      // System.Diagnostics.Stopwatch sw2 = new System.Diagnostics.Stopwatch();
      for (int i = 0; i < 100; ++i) {
        ExtendedRational er = CBORTest.RandomRational(fr);
        int exp = -100000 + fr.NextValue(200000);
        ExtendedDecimal ed = ExtendedDecimal.Create(
          CBORTest.RandomBigInteger(fr),
          BigInteger.valueOf(exp));
        ExtendedRational er2 = ExtendedRational.FromExtendedDecimal(ed);
        // sw1.Start();
        int c2r = er.compareTo(er2);
        // sw1.Stop();sw2.Start();
        int c2d = er.CompareToDecimal(ed);
        // sw2.Stop();
        Assert.assertEquals(c2r, c2d);
      }
      // System.out.println("compareTo: " + (sw1.getElapsedMilliseconds()/1000.0) + " s");
      // System.out.println("CompareToDecimal: " + (sw2.getElapsedMilliseconds()/1000.0) + " s");
    }

    @Test
    public void TestRationalDivide() {
      FastRandom fr = new FastRandom();
      for (int i = 0; i < 100; ++i) {
        ExtendedRational er = CBORTest.RandomRational(fr);
        ExtendedRational er2 = CBORTest.RandomRational(fr);
        if (er2.signum()==0 || !er2.isFinite()) {
          continue;
        }
        ExtendedRational ermult = er.Multiply(er2);
        ExtendedRational erdiv = ermult.Divide(er);
        if (erdiv.compareTo(er2) != 0) {
          Assert.fail(er + "; " + er2);
        }
        erdiv = ermult.Divide(er2);
        if (erdiv.compareTo(er) != 0) {
          Assert.fail(er + "; " + er2);
        }
      }
    }

    @Test
    public void TestRationalRemainder() {
      FastRandom fr = new FastRandom();
      for (int i = 0; i < 100; ++i) {
        ExtendedRational er;
        ExtendedRational er2;
        er = new ExtendedRational(CBORTest.RandomBigInteger(fr), BigInteger.ONE);
        er2 = new ExtendedRational(CBORTest.RandomBigInteger(fr), BigInteger.ONE);
        if (er2.signum()==0 || !er2.isFinite()) {
          continue;
        }
        ExtendedRational ermult = er.Multiply(er2);
        ExtendedRational erdiv = ermult.Divide(er);
        erdiv = ermult.Remainder(er);
        if (erdiv.signum()!=0) {
          Assert.fail(ermult + "; " + er);
        }
        erdiv = ermult.Remainder(er2);
        if (erdiv.signum()!=0) {
          Assert.fail(er + "; " + er2);
        }
      }
    }

    @Test
    public void TestRationalCompare() {
      FastRandom fr = new FastRandom();
      for (int i = 0; i < 100; ++i) {
        BigInteger num = CBORTest.RandomBigInteger(fr);
        if (num.signum()==0) {
          // Skip if number is 0; 0/1 and 0/2 are
          // equal in that case
          continue;
        }
        num = (num).abs();
        ExtendedRational rat = new ExtendedRational(num, BigInteger.ONE);
        ExtendedRational rat2 = new ExtendedRational(num, BigInteger.valueOf(2));
        if (rat2.compareTo(rat) != -1) {
          Assert.fail(rat + "; " + rat2);
        }
        if (rat.compareTo(rat2) != 1) {
          Assert.fail(rat + "; " + rat2);
        }
      }
      Assert.assertEquals(
        -1,
        new ExtendedRational(BigInteger.ONE, BigInteger.valueOf(2)).compareTo(
          new ExtendedRational(BigInteger.valueOf(4), BigInteger.ONE)));
      for (int i = 0; i < 100; ++i) {
        BigInteger num = CBORTest.RandomBigInteger(fr);
        BigInteger den = CBORTest.RandomBigInteger(fr);
        if (den.signum()==0) {
          den = BigInteger.ONE;
        }
        ExtendedRational rat = new ExtendedRational(num, den);
        for (int j = 0; j < 10; ++j) {
          BigInteger num2 = num;
          BigInteger den2 = den;
          BigInteger mult = CBORTest.RandomBigInteger(fr);
          if (mult.signum()==0 || mult.equals(BigInteger.ONE)) {
            mult = BigInteger.valueOf(2);
          }
          num2=num2.multiply(mult);
          den2=den2.multiply(mult);
          ExtendedRational rat2 = new ExtendedRational(num2, den2);
          if (rat.compareTo(rat2) != 0) {
            Assert.assertEquals(rat + "; " + rat2 + "; " + rat.ToDouble() + "; " + rat2.ToDouble(),0,rat.compareTo(rat2));
          }
        }
      }
    }

    @Test
    public void TestBuiltInTags() {
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, 0x20  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, 0x60  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, (byte)0x80  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, (byte)0xa0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, (byte)0xe0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }

      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x20  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x60  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, (byte)0x80  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, (byte)0xa0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, (byte)0xe0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }

      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, 0x20  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, 0x40  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, 0x60  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0x80  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xd8, 0x1e, (byte)0x9f, 0x01, 0x01, (byte)0xff  });
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xd8, 0x1e, (byte)0x9f, 0x01, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xd8, 0x1e, (byte)0x9f, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0x9f, 0x00, 0x00, (byte)0xff  });
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0x9f, 0x00, 0x00, (byte)0xff  });
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0x9f, 0x00, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0x9f, 0x00, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0x9f, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0x9f, (byte)0xff  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0x81, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0xa0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc4, (byte)0xe0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }

      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, 0x20  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, 0x40  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, 0x60  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0x80  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0x81, 0x00  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0xa0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc5, (byte)0xe0  });
        Assert.fail("Should have failed");
      } catch (CBORException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }

      Assert.assertEquals(BigInteger.ZERO, CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc2, 0x40  }).AsBigInteger());
      Assert.assertEquals(BigInteger.ZERO.subtract(BigInteger.ONE), CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x41, 0x00  }).AsBigInteger());
      Assert.assertEquals(BigInteger.ZERO.subtract(BigInteger.ONE), CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x40  }).AsBigInteger());
    }

    @Test
    public void TestUUID() {
      CBORObject obj = CBORObject.FromObject(java.util.UUID.fromString("00112233-4455-6677-8899-AABBCCDDEEFF"));
      Assert.assertEquals(CBORType.ByteString, obj.getType());
      Assert.assertEquals(BigInteger.valueOf(37), obj.getInnermostTag());
      byte[] bytes = obj.GetByteString();
      Assert.assertEquals(16, bytes.length);
      Assert.assertEquals(0x00, bytes[0]);
      Assert.assertEquals(0x11, bytes[1]);
      Assert.assertEquals(0x22, bytes[2]);
      Assert.assertEquals(0x33, bytes[3]);
      Assert.assertEquals(0x44, bytes[4]);
      Assert.assertEquals(0x55, bytes[5]);
      Assert.assertEquals(0x66, bytes[6]);
      Assert.assertEquals(0x77, bytes[7]);
      Assert.assertEquals((byte)0x88, bytes[8]);
      Assert.assertEquals((byte)0x99, bytes[9]);
      Assert.assertEquals((byte)0xaa, bytes[10]);
      Assert.assertEquals((byte)0xbb, bytes[11]);
      Assert.assertEquals((byte)0xcc, bytes[12]);
      Assert.assertEquals((byte)0xdd, bytes[13]);
      Assert.assertEquals((byte)0xee, bytes[14]);
      Assert.assertEquals((byte)0xff, bytes[15]);
    }

    // @Test
    public void TestMiniCBOR() {
      byte[] bytes;
      bytes = new byte[] {  0x19, 2  };
      try {
        MiniCBOR.ReadInt32(new java.io.ByteArrayInputStream(bytes));
        Assert.fail("Should have failed");
      } catch (IOException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      bytes = new byte[] {  0x1a, 2  };
      try {
        MiniCBOR.ReadInt32(new java.io.ByteArrayInputStream(bytes));
        Assert.fail("Should have failed");
      } catch (IOException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      bytes = new byte[] {  0x1b, 2  };
      try {
        MiniCBOR.ReadInt32(new java.io.ByteArrayInputStream(bytes));
        Assert.fail("Should have failed");
      } catch (IOException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      bytes = new byte[] {  0x1b, 2, 2, 2, 2, 2, 2, 2, 2  };
      try {
        MiniCBOR.ReadInt32(new java.io.ByteArrayInputStream(bytes));
        Assert.fail("Should have failed");
      } catch (IOException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      bytes = new byte[] {  0x1c, 2  };
      try {
        MiniCBOR.ReadInt32(new java.io.ByteArrayInputStream(bytes));
        Assert.fail("Should have failed");
      } catch (IOException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        bytes = new byte[] {  0  };
        java.io.ByteArrayInputStream ms=null;
try {
ms=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(0, MiniCBOR.ReadInt32(ms));
}
finally {
try { if(ms!=null)ms.close(); } catch (java.io.IOException ex){}
}
        bytes = new byte[] {  0x17  };
        java.io.ByteArrayInputStream ms2=null;
try {
ms2=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(0x17, MiniCBOR.ReadInt32(ms2));
}
finally {
try { if(ms2!=null)ms2.close(); } catch (java.io.IOException ex){}
}
        bytes = new byte[] {  0x18, 2  };
        java.io.ByteArrayInputStream ms3=null;
try {
ms3=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(2, MiniCBOR.ReadInt32(ms3));
}
finally {
try { if(ms3!=null)ms3.close(); } catch (java.io.IOException ex){}
}
        bytes = new byte[] {  0x19, 0, 2  };
        java.io.ByteArrayInputStream ms4=null;
try {
ms4=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(2, MiniCBOR.ReadInt32(ms4));
}
finally {
try { if(ms4!=null)ms4.close(); } catch (java.io.IOException ex){}
}
        bytes = new byte[] {  0x27  };
        java.io.ByteArrayInputStream ms5=null;
try {
ms5=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(-1 - 7, MiniCBOR.ReadInt32(ms5));
}
finally {
try { if(ms5!=null)ms5.close(); } catch (java.io.IOException ex){}
}
        bytes = new byte[] {  0x37  };
        java.io.ByteArrayInputStream ms6=null;
try {
ms6=new java.io.ByteArrayInputStream(bytes);

          Assert.assertEquals(-1 - 0x17, MiniCBOR.ReadInt32(ms6));
}
finally {
try { if(ms6!=null)ms6.close(); } catch (java.io.IOException ex){}
}
      }
      catch (IOException ioex) {
        Assert.fail(ioex.getMessage());
      }
    }

    @Test
    public void TestNegativeBigInts() {
      BigInteger minusone = BigInteger.ZERO.subtract(BigInteger.ONE);
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(8)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x42, 1, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(16)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x43, 1, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(24)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x44, 1, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(32)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x45, 1, 0, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(40)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x46, 1, 0, 0, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(48)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x47, 1, 0, 0, 0, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(56)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x48, 1, 0, 0, 0, 0, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(64)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x49, 1, 0, 0, 0, 0, 0, 0, 0, 0  }).AsBigInteger());
      Assert.assertEquals(
        minusone .subtract(BigInteger.ONE.shiftLeft(72)),
        CBORObject.DecodeFromBytes(new byte[] {  (byte)0xc3, 0x4a, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0  }).AsBigInteger());
    }

    @Test
    public void TestStringRefs() {
      CBORObject cbor = CBORObject.DecodeFromBytes(
        new byte[] {  (byte)0xd9,
        1,
        0,
        (byte)0x9f,
        0x64,
        0x61,
        0x62,
        0x63,
        0x64,
        (byte)0xd8,
        0x19,
        0x00,
        (byte)0xd8,
        0x19,
        0x00,
        0x64,
        0x62,
        0x62,
        0x63,
        0x64,
        (byte)0xd8,
        0x19,
        0x01,
        (byte)0xd8,
        0x19,
        0x00,
        (byte)0xd8,
        0x19,
        0x01,
        (byte)0xff  });
      String expected = "[\"abcd\",\"abcd\",\"abcd\",\"bbcd\",\"bbcd\",\"abcd\",\"bbcd\"]";
      Assert.assertEquals(expected, cbor.ToJSONString());
      cbor = CBORObject.DecodeFromBytes(
        new byte[] {  (byte)0xd9,
        1,
        0,
        (byte)0x9f,
        0x64,
        0x61,
        0x62,
        0x63,
        0x64,
        0x62,
        0x61,
        0x61,
        (byte)0xd8,
        0x19,
        0x00,
        (byte)0xd8,
        0x19,
        0x00,
        0x64,
        0x62,
        0x62,
        0x63,
        0x64,
        (byte)0xd8,
        0x19,
        0x01,
        (byte)0xd8,
        0x19,
        0x00,
        (byte)0xd8,
        0x19,
        0x01,
        (byte)0xff  });
      expected = "[\"abcd\",\"aa\",\"abcd\",\"abcd\",\"bbcd\",\"bbcd\",\"abcd\",\"bbcd\"]";
      Assert.assertEquals(expected, cbor.ToJSONString());
    }

    @Test
    public void TestExtendedNaNZero() {
      if(ExtendedDecimal.NaN.signum()==0)Assert.fail();
      if(ExtendedDecimal.SignalingNaN.signum()==0)Assert.fail();
      if(ExtendedFloat.NaN.signum()==0)Assert.fail();
      if(ExtendedFloat.SignalingNaN.signum()==0)Assert.fail();
      if(ExtendedRational.NaN.signum()==0)Assert.fail();
      if(ExtendedRational.SignalingNaN.signum()==0)Assert.fail();
    }

    @Test
    public void TestToBigIntegerNonFinite() {
      try {
        ExtendedDecimal.PositiveInfinity.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedDecimal.NegativeInfinity.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedDecimal.NaN.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedDecimal.SignalingNaN.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedFloat.PositiveInfinity.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedFloat.NegativeInfinity.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedFloat.NaN.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
      try {
        ExtendedFloat.SignalingNaN.ToBigInteger();
        Assert.fail("Should have failed");
      } catch (ArithmeticException ex) {
      } catch (Exception ex) {
        Assert.fail(ex.toString());
        throw new IllegalStateException("", ex);
      }
    }
  }