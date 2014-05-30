package com.upokecenter.cbor;
/*
Written in 2014 by Peter O.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/
 */

import com.upokecenter.util.*;

  class CBORTag5 implements ICBORTag
  {
    private static CBORTypeFilter valueFilter = new CBORTypeFilter().WithArrayExactLength(
      2,
      CBORTypeFilter.UnsignedInteger.WithNegativeInteger(),
      CBORTypeFilter.UnsignedInteger.WithNegativeInteger().WithTags(2, 3));

    public CBORTypeFilter GetTypeFilter() {
      return valueFilter;
    }

    static CBORObject ConvertToDecimalFrac(CBORObject o, boolean isDecimal) {
      if (o.getType() != CBORType.Array) {
        throw new CBORException("Big fraction must be an array");
      }
      if (o.size() != 2) {
        throw new CBORException("Big fraction requires exactly 2 items");
      }
      if (!o.get(0).isIntegral()) {
        throw new CBORException("Exponent is not an integer");
      }
      if (!o.get(1).isIntegral()) {
        throw new CBORException("Mantissa is not an integer");
      }
      BigInteger exponent = o.get(0).AsBigInteger();
      BigInteger mantissa = o.get(1).AsBigInteger();
      if (exponent.bitLength() > 64) {
        throw new CBORException("Exponent is too big");
      }
      if (exponent.signum()==0) {
        // Exponent is 0, so return mantissa instead
        return CBORObject.FromObject(mantissa);
      }
      // NOTE: Discards tags. See comment in CBORTag2.
      if (isDecimal) {
        return CBORObject.FromObject(ExtendedDecimal.Create(mantissa, exponent));
      } else {
        return CBORObject.FromObject(ExtendedFloat.Create(mantissa, exponent));
      }
    }

    public CBORObject ValidateObject(CBORObject obj) {
      return ConvertToDecimalFrac(obj, false);
    }
  }
