package com.upokecenter.util;
/*
 * Created by SharpDevelop.
 * User: Peter
 * Date: 2/26/2014
 * Time: 11:48 PM
 *
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */

    /**
     * Description of ICBORConverter.
     * @param <T> Type to convert to a CBOR object.
     */
  public interface ICBORConverter<T>
  {
    CBORObject ToCBORObject(T obj);
  }