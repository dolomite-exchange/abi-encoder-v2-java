package com.esaulpaugh.headlong.rlp.example;

import com.esaulpaugh.headlong.rlp.DecodeException;
import com.esaulpaugh.headlong.rlp.RLPList;
import com.esaulpaugh.headlong.rlp.util.ObjectNotation;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.esaulpaugh.headlong.rlp.RLPDecoder.RLP_LENIENT;

public class StudentTest {

    private static final byte[] STUDENT_RLP_SEQUENTIAL = Hex.decode("85506c61746f84460ca00ab88a3232b0883839e5de6a8bf0555b6304b703041e82fe7568aa8b6837aa62740a83fe5aaa8736a1c2a27080f77142702cdf4a81ca2744bda44397bbd58c63f35c0eb6796bf485d750a0b9bfa4a2f3be5b9030a7f2b13d6a4d468e22b32fe92506b11af5517d425bc68f26f2525a61f1a954c50933874c7d97b1cd8ff65d55f651cb7c455876278787ac3a40b4269da3281d03da142f61ba27534caabf68f4cf30bcd23399b8cdf6fddc601f76012819f4572f901661ec6a5122f901661ec6a51279");

    private static final byte[] STUDENT_RLP_LIST = Hex.decode("f8cd85506c61746f84460ca00ab88a3232b0883839e5de6a8bf0555b6304b703041e82fe7568aa8b6837aa62740a83fe5aaa8736a1c2a27080f77142702cdf4a81ca2744bda44397bbd58c63f35c0eb6796bf485d750a0b9bfa4a2f3be5b9030a7f2b13d6a4d468e22b32fe92506b11af5517d425bc68f26f2525a61f1a954c50933874c7d97b1cd8ff65d55f651cb7c455876278787ac3a40b4269da3281d03da142f61ba27534caabf68f4cf30bcd23399b8cdf6fddc601f76012819f4572f901661ec6a5122f901661ec6a51279");

    private static final String STUDENT_TO_STRING = "Plato, 9000.01, 42614923710474099051865073204632262589579065351888983454633476946624606237920155844117624881925308800846339143726224240899171443679555954603453329411015471777430813363275539775856672253880435777567715200132619026358726307537867398362020608423752638708533909946520930698520947692165070514401896564444792357016643365814597705551067712, $2552.7185792349726775956284153005464480874316939890710382513661202185792349726775956284153005464480874316939890710382513661202";

    @Test
    public void decodeEncode() throws DecodeException {
        Student plato = new Student(STUDENT_RLP_SEQUENTIAL, 0);
        Assert.assertEquals(STUDENT_TO_STRING, plato.toString());

        byte[] rlp = plato.toRLP();
        Assert.assertArrayEquals(STUDENT_RLP_SEQUENTIAL, rlp);


    }

    @Test
    public void adapterDecodeEncode() throws DecodeException {

        StudentRLPAdapter adapter = new StudentRLPAdapter();

        Student plato = adapter.decode(STUDENT_RLP_LIST);

        Assert.assertEquals(STUDENT_TO_STRING, plato.toString());

        byte[] rlp = adapter.encode(plato);
//        System.out.println(Hex.toHexString(rlp));

        Assert.assertArrayEquals(STUDENT_RLP_LIST, rlp);
    }

    @Test
    public void miniDecodeBenchmark() throws DecodeException {
        Student plato;
        StudentRLPAdapter adapter = new StudentRLPAdapter();
        byte[] rlp = STUDENT_RLP_SEQUENTIAL;
        byte[] temp = new byte[rlp.length];

        final int n = 10_000_000;

        System.out.println("Doing " + new DecimalFormat("#,###").format(n) + " decodes of Student object:\n" + ObjectNotation.forEncoding(rlp));

        long start, end;

        // warmup
        for (int i = 0; i < 5_500_000; i++) {
            plato = new Student(rlp, 0);
//            plato.toRLP(temp, 0);
//            rlp = adapter.encode(plato);
//            plato = adapter.decode(rlp);
        }
        start = System.nanoTime();
        for (int i = 0; i < n; i++) {
            plato = new Student(rlp, 0);
//            plato.toRLP(temp, 0);
//            rlp = adapter.encode(plato);
//            plato = adapter.decode(rlp);
        }
        end = System.nanoTime();

//        Assert.assertArrayEquals(STUDENT_RLP_SEQUENTIAL, temp);

        System.out.println(((end - start) / 1000000.0) + " millis");

    }

    @Test
    public void miniBenchmark() throws DecodeException {
        byte[] rlp = Hex.decode("f8cbf8c7a00000000000000000000000000000000000000000000000000000000000000000a01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347940000000000000000000000000000000000000000a02f4399b08efe68945c1cf90ffe85bbe3ce978959da753f9e649f034015b8817da00000000000000000000000000000000000000000000000000000000000000000834000008080830f4240808080a004994f67dc55b09e814ab7ffc8df3686b4afb2bb53e60eae97ef043fe03fb829c0c0");

        final int n = 10_000_000;

        RLPList rlpList;

        System.out.println("Doing " + new DecimalFormat("#,###").format(n) + " decodes of Student object:\n" + ObjectNotation.forEncoding(rlp));

        long start, end;

        List<Object> results0 = new ArrayList<>();
        rlpList = (RLPList) RLP_LENIENT.wrap(rlp);
        rlpList.elements(RLP_LENIENT);
//        rlpList.elementsRecursive(results0, RLP_LENIENT);

//        List<Object> results0 = new ArrayList<>();
//        rlpList = (RLPList) RLP_LENIENT.wrap(rlp);
//        collectAll(results0, rlpList);

        // warmup
        for (int i = 0; i < 5_500_000; i++) {
//            List<Object> results = new ArrayList<>();
            rlpList = (RLPList) RLP_LENIENT.wrap(rlp);
            rlpList.elements(RLP_LENIENT);
//            rlpList.elementsRecursive(results, RLP_LENIENT);
        }
        start = System.nanoTime();
        for (int i = 0; i < n; i++) {
//            List<Object> results = new ArrayList<>();
            rlpList = (RLPList) RLP_LENIENT.wrap(rlp);
            rlpList.elements(RLP_LENIENT);
//            rlpList.elementsRecursive(results, RLP_LENIENT);
        }
        end = System.nanoTime();

        System.out.println(((end - start) / 1000000.0) + " millis");
    }

//    private void collectAll(List<Object> results, RLPList rlpList) throws DecodeException {
//        List<RLPItem> actualList = rlpList.elements(RLP_LENIENT);
//        for (RLPItem element : actualList) {
//            if(element.isList()) {
//                List<Object> subList = new ArrayList<>();
//                collectAll(subList, (RLPList) element);
//                results.add(subList);
//            } else {
//                results.add(element);
//            }
//        }
//    }

}