package StatisticsVer1;
/**
 *EdaSearch.java ver1
 * テキストにある文字から数字列に分解し、
 * EDAによる外れ値の除外を行い5大要約値を出力。
 * なお、計算面倒で分散周りの検算はしてないです(｀・ω・´)
 *
 * 製作日数：2日
 *
 * 改善したいことⅠ：クラス化(重要)
 * 改善したいことⅡ：命名規則の統一
 * 改善したいことⅢ：staticメソッドの理解を深めて正しい理解をしたい
 * 改善したいことⅣ：正規表現をちゃんと学習したい
 *
 * 反省点Ⅰ：StringBuilderの使い方が馬鹿の一つ覚えでappendしてたので何とかしたい所存(;´･ω･)
 * 反省点Ⅱ：OutOfBoundsを多発させたのは反省点として大きい。lengthやsizeで取った値と添え字の最大値はズレがあることを忘れてはならない。
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ErrorValueElimination {
    public static void main(String[] args) {
        Scanner scanSysIn = new Scanner(System.in);
        int[] quartileArray = new int[3];

        System.out.print("小数点の位数：");
        int intRange = scanSysIn.nextInt();

        System.out.print("入力用ファイル名設定：");
        String fileInputName = scanSysIn.next();

        System.out.print("出力用のファイル名設定：");
        String fileOutputName = scanSysIn.next();
        scanSysIn.close();

        StringBuilder sbPrint = new StringBuilder();
        ArrayList<Integer> intListSpray = new ArrayList<>();

        readTextIntList(intListSpray, fileInputName);

        System.out.println("------------------------------ソート開始------------------------------");
        printList(intListSpray);
        Collections.sort(intListSpray); //ソート処理
        System.out.println("------------------------------ソート終了------------------------------");
        printList(intListSpray);

        quartileSearch(intListSpray, quartileArray); //四分位数捜索
        print5Datas(intListSpray.get(0), intListSpray.get(intListSpray.size() - 1), quartileArray);
        outliersExcept(intListSpray, quartileArray); //EDAによる外れ値削除
        System.out.println();

        quartileSearch(intListSpray, quartileArray);
        print5Datas(intListSpray.get(0), intListSpray.get(intListSpray.size() - 1), quartileArray);

        System.out.println("----------------------------------------------------------------------");

        sbPrint.append("外れ値除外後：");
        for (int intTemp : intListSpray) {
            sbPrint.append(Integer.toString(intTemp) + "\t");
        }
        sbPrint.append("\n");

        double[] deviationValueArray = new double[intListSpray.size()]; //偏差値を格納する配列

        double avgSpray = 0;
        for (int tmp : intListSpray) {
            avgSpray += (double)tmp; // Σ
        }
        sbPrint.append("合計値 : ");
        sbPrint.append(Integer.toString((int)avgSpray));

        avgSpray /= (double)intListSpray.size(); // 1/n * Σ
        sbPrint.append("\t平均値 : ");
        sbPrint.append(Double.toString(avgSpray));

        sbPrint.append("\t分散：");
        double doubVarFromListToStandardDeviation = doubVarianceFromList(intListSpray, avgSpray); // σ^2計算
        sbPrint.append(Double.toString(doubVarFromListToStandardDeviation));

        sbPrint.append("\t標準偏差：");
        doubVarFromListToStandardDeviation = Math.sqrt(doubVarFromListToStandardDeviation);// √σ^2 = σ
        sbPrint.append(Double.toString(doubVarFromListToStandardDeviation));

        sbPrint.append("\n偏差値：");// 50 + (avg - Xi) / σ
        setDeviationValue(intListSpray, doubVarFromListToStandardDeviation, avgSpray, intRange, deviationValueArray);
        for (double printTmp : deviationValueArray) { // 作成した偏差値を格納
            sbPrint.append(Double.toString(printTmp));
            sbPrint.append("\t");
        }

        writeStrBuild(sbPrint, fileOutputName);

        System.out.println(sbPrint.toString());
    }

    // 配列内のデータを表示するメソッド
    public static void printList (List<Integer> intVector) {
        StringBuilder sbPrint = new StringBuilder();

        for (int intTmp : intVector) {
            sbPrint.append(Integer.toString(intTmp));
            sbPrint.append("\t");
        }
        System.out.println(sbPrint.toString());
    }

    // EDA法による外れ値除外するメソッド
    public static void outliersExcept(List<Integer> intVector, int[] quaAr) {
        final int IQR1_5 = (int)Math.round(1.5 * (quaAr[2] - quaAr[0]));

        int intEdaMin = quaAr[0] - IQR1_5;
        int intEdaMax = quaAr[2] + IQR1_5;

        System.out.println("許容最小値：" + intEdaMin + "\t\t\t\t\t\t許容最大値：" + intEdaMax);

        for (int numMin = 0; intVector.get(numMin) < intEdaMin; numMin++) {
            intVector.remove(0);
        }
        for (int numMax = intVector.size() - 1; intVector.get(numMax) > intEdaMax; numMax--) {
            intVector.remove(numMax);
        }
    }

    //5大要約数を表示するメソッド
    public static void print5Datas(int max, int min, int[] qAr){
        StringBuilder st5Datas = new StringBuilder();
        st5Datas.append("最小値：");
        st5Datas.append(max);
        st5Datas.append("   第1四分位数：");
        st5Datas.append(qAr[0]);
        st5Datas.append("   中央値：");
        st5Datas.append(qAr[1]);
        st5Datas.append("   第3四分位数：");
        st5Datas.append(qAr[2]);
        st5Datas.append("   最大値：");
        st5Datas.append(min);
        System.out.println(st5Datas.toString());
    }

    //第一から第三四分位数までの値を配列に格納するメソッド
    public static void quartileSearch (List<Integer> intList, int[] intStorage) {
        int size = intList.size() - 1;
        intStorage[0] = intList.get((int)Math.round(size / 4.0));
        intStorage[1] = size % 2 == 0 ? intList.get((int)((size / 2.0) + 0.5)) : (int)(Math.round((intList.get(size / 2 + 1) + intList.get(size / 2)) / 2.0));
        intStorage[2] = intList.get((int)Math.round(size * 3 / 4.0));
    }

    //分散を求め、doubleで返すメソッド
    public static double doubVarianceFromList (List<Integer> intList, double doubAvg) {
        double ans = 0;

        for (int temp : intList) {
            ans += Math.pow(temp, 2.0); //Σ Xi^2...Ⅰ
        }
        ans -= intList.size() * Math.pow(doubAvg, 2.0);// Ⅰ - n * avg^2

        return ans;
    }

    // 偏差値を求めて、配列に格納して返すメソッド
    public static void setDeviationValue (List<Integer> intList, double doubSD, double doubAvg,int accuracy, double[] doubAnsArray) {
        int range = (int)Math.pow(10, accuracy);
        for (int idx = 0; idx < doubAnsArray.length; idx++) {
            doubAnsArray[idx] = Math.floor((50 + ((intList.get(idx) - doubAvg) / doubSD) * 10) * range) / range;
        }
    }

    // カレントディレクトリのPathを取得
    public static String getAbsPath() {
        String path = new File("src").getAbsolutePath();
        // System.out.println("abs path" + path);
        return path;
    }

    // テキストからデータを取得し、int型で格納する(StringToIntegerの例外は考慮しない)
    public static void readTextIntList (List<Integer> intList, String fileName){
        try {
            String strAbsPath = getAbsPath();
            File fileEdaDataPath = new File(strAbsPath.concat("\\" + fileName));

            BufferedReader brDataText = new BufferedReader(new FileReader(fileEdaDataPath));//FileReaderはtryでエラー時の処理を書かないとエラーになってしまう。
            String strTemp;
            while((strTemp = brDataText.readLine()) != null) {
                String[] bufStrArray = strTemp.trim().split(" +");
                for (String strToIntTemp : bufStrArray) {
                    intList.add(Integer.parseInt(strToIntTemp));
                }
            }
            brDataText.close();
        }
        catch(FileNotFoundException error) {
            System.out.println(error);
        }
        catch(IOException error){
            System.out.println(error);
        }
    }

    //builderに格納した内容を、fileNameの場所に書き込む
    public static void writeStrBuild(StringBuilder sbStr, String fileName) {
        try{
            String strAbsPath = getAbsPath();
            File fileOutputPath = new File(strAbsPath.concat("\\" + fileName));

            FileWriter fwStr = new FileWriter(fileOutputPath);
            fwStr.write(sbStr.toString());

            fwStr.close();
        }
        catch(FileNotFoundException error) {
            System.out.println(error);
        }
        catch(IOException error) {
            System.out.println(error);
        }
    }
}