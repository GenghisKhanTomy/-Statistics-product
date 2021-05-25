package StatisticsVer1;

/**
 *EdaSearch.java ver2
 * テキストにある文字から数字列に分解し、
 * EDAによる外れ値の除外を行い5大要約値を出力。
 * なお、計算面倒で分散周りの検算はしてないです(｀・ω・´)
 *
 * 製作日数：2日 + 1日
 *
 * 改善箇所：
 * ・クラス・コンストラクタの実装
 * 
 * 改善したいことⅠ：命名規則の統一(重要)
 * 改善したいことⅡ：正規表現をちゃんと学習したい
 *
 * 反省点Ⅰ：クラス化した結果、行数が100行増加し、可読性が下がった気がする。
 *          本来はクラスファイルに分けたかったが、クラスファイルでの管理方法を知らないので追々改善したい。
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

class TextIO{
    String fileInputName;
    String fileOutputName;

    public TextIO() { //デフォルトコンストラクタ
        this.fileInputName = "numData.text";
        this.fileOutputName = "ansDatas.text";
        // System.out.println("default");
    }

    public TextIO(Scanner scan) { //コンストラクタ・オーバーロード
        setInputPath(scan);
        setOutputPath(scan);
    }

    public void setInputPath(Scanner scan) {
        System.out.print("入力用ファイル名設定 (現在：" + fileInputName + ") ：");
        String fileInputName = scan.next();
        System.out.println("変更後：" + fileInputName);
    }

    public void setOutputPath(Scanner scan) {
        System.out.print("出力用のファイル名設定 (現在：" + fileInputName + ") ：");
        String fileOutputName = scan.next();
        System.out.println("変更後：" + fileOutputName);
    }
    // カレントディレクトリのPathを取得
    public String getAbsPath() {
        String path = new File("src").getAbsolutePath();
        // System.out.println("abs path" + path);
        return path;
    }

    // テキストからデータを取得し、int型で格納する(StringToIntegerの例外は考慮しない)
    public void readTextIntList (List<Integer> intList){
        try {
            String strAbsPath = getAbsPath();
            // System.out.println(strAbsPath);
            File fileEdaDataPath = new File(strAbsPath.concat("\\" + this.fileInputName));

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
    public void writeStrBuild(StringBuilder sbStr) {
        try{
            String strAbsPath = getAbsPath();
            File fileOutputPath = new File(strAbsPath.concat("\\" + this.fileOutputName));

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



class StatisticsMath {
    private int intAccuracy;

    public StatisticsMath(List<Integer> intList) {
        System.out.print("小数点の位数は3桁です。");
        intAccuracy = 3;
    }

    public StatisticsMath(List<Integer> intList, Scanner scan) {
        System.out.print("小数点の位数：");
        this.intAccuracy = scan.nextInt();
    }

    //合計
    public int intListSum (List<Integer> intList) {
        int ans = 0;
        for (int data : intList) {
            ans += data;
        }
        return ans;
    }

    //平均
    public double intListAvg (List<Integer> intList) {
        double ans = intListSum(intList) / intList.size();
        return ans;
    }

    //第一から第三四分位数までの値を配列に格納するメソッド
    public void quartileSearch (List<Integer> intList, int[] intStorage) {
        int size = intList.size() - 1;
        intStorage[0] = intList.get((int)Math.round(size / 4.0));
        intStorage[1] = size % 2 == 0 ? intList.get((int)((size / 2.0) + 0.5)) : (int)(Math.round((intList.get(size / 2 + 1) + intList.get(size / 2)) / 2.0));
        intStorage[2] = intList.get((int)Math.round(size * 3 / 4.0));
    }

    //偏差平方和
    public double doubListVarianceSum (List<Integer> intList) {
        double ans = 0;
        for (int temp : intList) {
            ans += Math.pow(temp - intListAvg(intList), 2.0); //Σ (Xi - avg)^2...
        }
        return ans;
    }

    //分散
    public double doubListVariance (List<Integer> intList) {
        double ans = doubListVarianceSum(intList) / intListAvg(intList);
        return ans;
    }

    //標準偏差
    public double doubListToStandardDeviation (List<Integer> intList) {
    double ans = Math.sqrt(doubListVariance(intList));// √σ^2 = σ
    return ans;
    }

    // 偏差値
    public void getDeviationValue (List<Integer> intList, List<Double> doubList) {// 50 + (avg - Xi) / σ
        int range = (int)Math.pow(10, this.intAccuracy);
        for (int idx = 0; idx < intList.size(); idx++) {
            doubList.add(Math.floor((50 + ((intList.get(idx) - intListAvg(intList)) / doubListToStandardDeviation(intList)) * 10) * range) / range);
        }
    }
}



class ErrorValueEliminationList {
    // EDA法による外れ値除外するメソッド
    public void outliersExcept(List<Integer> intVector, int[] quaAr) {
        final int IQR1_5 = (int)Math.round(1.5 * (quaAr[2] - quaAr[0]));

        int intEdaMin = quaAr[0] - IQR1_5;
        int intEdaMax = quaAr[2] + IQR1_5;

        System.out.println("許容最小値：" + intEdaMin + "      許容最大値：" + intEdaMax);

        for (int numMin = 0; intVector.get(numMin) < intEdaMin; numMin++) {
            intVector.remove(0);
        }
        for (int numMax = intVector.size() - 1; intVector.get(numMax) > intEdaMax; numMax--) {
            intVector.remove(numMax);
        }
    }
}



class ListPrint {
    StringBuilder sb = new StringBuilder();
    
    // リスト内のデータをStringBulderに格納
    public void storageIntList (List<Integer> intList, StringBuilder sb) {
        for (int intTmp : intList) {
            sb.append(Integer.toString(intTmp));
            sb.append("  ");
        }
        sb.append("\n");
    }

    // リスト内のデータを表示するメソッド
    public void printIntList(List<Integer> intList) { 
        storageIntList(intList, this.sb);
        System.out.print(this.sb.toString());
        this.sb.setLength(0);
    }

    public void storageDoubList (List<Double> doubList, StringBuilder sb) {
        for (double printTmp : doubList) {
            sb.append(Double.toString(printTmp));
            sb.append("  ");
        }
    }

    public void printDoubList(List<Double> intList) { 
        storageDoubList(intList, this.sb);
        System.out.println(this.sb.toString());
        this.sb.setLength(0);
    }

    //5大要約数をStringBuilder
    public void storageRepresentativeVars(StringBuilder sb, int max, int min, int[] qAr){
        sb.append("最小値：");
        sb.append(max);
        sb.append("   第1四分位数：");
        sb.append(qAr[0]);
        sb.append("   中央値：");
        sb.append(qAr[1]);
        sb.append("   第3四分位数：");
        sb.append(qAr[2]);
        sb.append("   最大値：");
        sb.append(min);
    }

    //5大要約数を表示するメソッド
    public void printRepresentativeVars(int max, int min, int[] qAr) {
        storageRepresentativeVars(this.sb, max, min, qAr);
        System.out.println(this.sb.toString());
        this.sb.setLength(0);
    }
}



public class ErrorValueEliminationStatistics {
    public static void main(String[] args) {
        // テキスト読み込み
        Scanner scanSysIn = new Scanner(System.in);
        TextIO textIOSysIn = new TextIO();
        ArrayList<Integer> intListSpray = new ArrayList<>();
        textIOSysIn.readTextIntList(intListSpray);

        // 統計処理準備
        int[] quartileArray = new int[3];
        StatisticsMath listStaMath = new StatisticsMath(intListSpray);
        StringBuilder sbPrint = new StringBuilder();
        ErrorValueEliminationList intListEVE = new ErrorValueEliminationList();
        ArrayList<Double> doubDVBuff = new ArrayList<>();
        ListPrint lstpr = new ListPrint();
        // System.out.println(intListSpray);


        Collections.sort(intListSpray); //ソート処理
        listStaMath.quartileSearch(intListSpray, quartileArray); //四分位数捜索
        lstpr.printIntList(intListSpray);
        lstpr.printRepresentativeVars(intListSpray.get(0), intListSpray.get(intListSpray.size() - 1), quartileArray);

        intListEVE.outliersExcept(intListSpray, quartileArray); //EDAによる外れ値削除

        listStaMath.quartileSearch(intListSpray, quartileArray);//外れ値除外後の四分位
        System.out.println("------------------------------外れ値除外前後での四分位範囲-----------------------------");
        lstpr.storageIntList(intListSpray, sbPrint);

        lstpr.storageRepresentativeVars(sbPrint, intListSpray.get(0), intListSpray.get(intListSpray.size() - 1), quartileArray);
        sbPrint.append("\n");

        sbPrint.append("合計値 : ");
        sbPrint.append(Integer.toString(listStaMath.intListSum(intListSpray)));

        sbPrint.append("  平均値 : ");
        sbPrint.append(Double.toString(listStaMath.intListAvg(intListSpray)));

        sbPrint.append("  分散：");
        sbPrint.append(Double.toString(listStaMath.doubListVariance(intListSpray)));

        sbPrint.append("  標準偏差：");
        sbPrint.append(Double.toString(listStaMath.doubListToStandardDeviation(intListSpray)));

        sbPrint.append("\n偏差値：\n");
        listStaMath.getDeviationValue(intListSpray, doubDVBuff);
        lstpr.storageDoubList(doubDVBuff, sbPrint);

        textIOSysIn.writeStrBuild(sbPrint);

        System.out.println(sbPrint.toString());
        scanSysIn.close();
    }
}