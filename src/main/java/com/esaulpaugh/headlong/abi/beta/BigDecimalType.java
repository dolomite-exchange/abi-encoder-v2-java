package com.esaulpaugh.headlong.abi.beta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

class BigDecimalType extends AbstractInt256Type<BigDecimal> {

    static final String CLASS_NAME = BigDecimal.class.getName();
    static final String CLASS_NAME_ELEMENT = BigDecimal[].class.getName().replaceFirst("\\[", "");

    final int scale;

    BigDecimalType(String canonicalAbiType, String className, int bitLength, int scale, boolean signed) {
        super(canonicalAbiType, className, bitLength, signed);
        this.scale = scale;
    }

    @Override
    BigDecimal decode(byte[] buffer, int index) {
        BigInteger bi = new BigInteger(Arrays.copyOfRange(buffer, index, index + INT_LENGTH_BYTES));
        BigDecimal dec = new BigDecimal(bi, scale);
        validateBigIntBitLen(bi);
        return dec;
    }

    @Override
    void validate(Object object) {
        super.validate(object);
        BigDecimal dec = (BigDecimal) object;
        validateBigIntBitLen(dec.unscaledValue());
        if(dec.scale() != scale) {
            throw new IllegalArgumentException("big decimal scale mismatch: actual != expected: " + dec.scale() + " != " + scale);
        }
    }
}
