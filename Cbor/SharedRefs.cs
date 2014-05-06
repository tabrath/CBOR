/*
Written by Peter O. in 2014.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/
 */
using System;
using System.Collections.Generic;
using PeterO;

namespace PeterO.Cbor {
    /// <summary>Description of SharedRefs.</summary>
  internal class SharedRefs
  {
    private IList<CBORObject> sharedObjects;
    private int refCount;

    public SharedRefs() {
      this.sharedObjects = new List<CBORObject>();
    }

    /// <summary>Not documented yet.</summary>
    /// <param name='obj'>A CBORObject object.</param>
    public void AddObject(CBORObject obj) {
      if (this.refCount > 0) {
        this.sharedObjects.Add(obj);
        --this.refCount;
      }
    }

    /// <summary>Not documented yet.</summary>
    /// <param name='smallIndex'>A 64-bit signed integer.</param>
    /// <returns>A string object.</returns>
    public CBORObject GetObject(long smallIndex) {
      if (smallIndex < 0) {
        throw new CBORException("Unexpected index");
      }
      if (smallIndex > Int32.MaxValue) {
        throw new CBORException("Index " + smallIndex + " is bigger than supported");
      }
      int index = (int)smallIndex;
      if (index >= this.sharedObjects.Count) {
        throw new CBORException("Index " + index + " is not valid");
      }
      return this.sharedObjects[index];
    }

    /// <summary>Not documented yet.</summary>
    /// <returns>A string object.</returns>
    /// <param name='bigIndex'>A BigInteger object.</param>
    public CBORObject GetObject(BigInteger bigIndex) {
      if (bigIndex.Sign < 0) {
        throw new CBORException("Unexpected index");
      }
      if (!bigIndex.canFitInInt()) {
        throw new CBORException("Index " + bigIndex + " is bigger than supported");
      }
      int index = (int)bigIndex;
      if (index >= this.sharedObjects.Count) {
        throw new CBORException("Index " + index + " is not valid");
      }
      return this.sharedObjects[index];
    }
  }
}