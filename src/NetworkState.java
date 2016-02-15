import java.util.Random;

/**
 * Created by tomohiro on 2016/02/12.
 */
public class NetworkState {
    int layerNum;
    public NetworkObject[] layer;
    double[][][] data;
    int midUnitNum;
    int outputNum;
    double[][] answer;
    char[] CS;
    double ALPHA = 0.01;
    double ALPHA_MID =0.1;
    double ERROR_MIN =10;
    static double[] outnum;
    int midOutNum=10;
    public NetworkState(double[][][] teacher,int layerNum, int midUnitNum, double[][] answer,char[] CS) {
        data = teacher;
        this.layerNum = layerNum;
        this.midUnitNum=midUnitNum;
        this.outputNum=answer[0].length;
        this.answer=answer;
        this.CS=CS;
        outnum = new double[outputNum];
        layer = new NetworkObject[Main.LAYERNUM];
        layer[0] = new NetworkObject(teacher[0][0].length, midUnitNum);

        //for (int i = 1; i < layerNum - 2; i++) {
        for(int i=1;i<layerNum-1;i++){
            layer[i] = new NetworkObject(midUnitNum, midUnitNum);
        }
        //layer[layerNum - 2] = new NetworkObject(midUnitNum,midOutNum);
        //layer[layerNum - 1] = new NetworkObject(midOutNum, outputNum);
        //layer[layerNum - 2] = new NetworkObject(midUnitNum,midOutNum);
        layer[layerNum - 1] = new NetworkObject(midUnitNum, outputNum);
    }

    public void calc(double[] data,boolean flag){
        for(int n=0;n<layer[0].node.length;n++){
            layer[0].node[n]=0;
            for(int j=0;j<data.length;j++){
                layer[0].node[n]+=data[j]*layer[0].weightObj[n].weight[j];
            }
            //layer[0].e[n]=layer[0].node[n];
            layer[0].node[n]=sigmoid(layer[0].node[n]);
        }

        for(int i=1;i<layerNum;i++) {
            for (int n = 0; n < layer[i].node.length; n++) {
                layer[i].node[n] = 0;
                for (int j = 0; j < layer[i-1].node.length; j++) {
                    layer[i].node[n] += layer[i-1].node[j] * layer[i].weightObj[n].weight[j];
                }
                //layer[i].e[n]=layer[0].node[n];
                layer[i].node[n] = sigmoid(layer[i].node[n]);
            }
        }

        if(flag==true){
            for(int i=0;i<layer[layerNum-1].node.length;i++){
                outnum[i]=layer[layerNum-1].node[i];
                System.out.printf("%4.4f\n", outnum[i]);
            }
        }
    }

    void BackPropagationCalc() {
        for(;;) {
            double e=0;
            for (int s = 0; s < Main.SAMPLE; s++) {
                for (int n = 0; n < CS.length; n++) {
                    calc(data[n][s], false);
                    e+=BackPropagation(data[n][s],answer[n]);
                }
            }
            System.out.printf("%5.20f\n",e);
            if(ERROR_MIN>e){break;}

            for(int j=0;j<layer[layerNum-2].weightObj.length;j++) {
                System.out.print(" " + layer[layerNum-1].weightObj[0].weight[j]);
            }
            System.out.println();
        }
    }

    public double BackPropagation(double[] data,double[] answer) {
        double e;

        for (int i = 0; i < answer.length; i++) {
            layer[layerNum - 1].delta[i] = (answer[i]-layer[layerNum - 1].node[i]) * layer[layerNum - 1].node[i] * (1 - layer[layerNum - 1].node[i]);
        }

        for (int i = layerNum - 2; i >= 0; i--) {//layer roop decrement
            for (int j = 0; j < layer[i].node.length; j++) {//current node num increment
                layer[i].delta[j] = 0;
                for (int k = 0; k < layer[i + 1].node.length; k++) {//output layer node num incremnt
                    layer[i].delta[j] += layer[i + 1].delta[k] * layer[i + 1].weightObj[k].weight[j] * layer[i].node[j] * (1 - layer[i].node[j]);
                }
            }
        }
        for (int i = 1; i < layerNum; i++) {//layer
            for (int j = 0; j < layer[i - 1].node.length; j++) {//left node num
                for (int k = 0; k < layer[i].node.length; k++) {//right node num
                    if(i==layerNum-1) {
                        layer[i].weightObj[k].weight[j] += ALPHA * layer[i].delta[k] * layer[i - 1].node[j];
                    }
                    else{
                        layer[i].weightObj[k].weight[j] += ALPHA_MID * layer[i].delta[k] * layer[i - 1].node[j];
                    }
                }
            }
        }

        for (int j = 0; j < data.length; j++) {//
            for (int k = 0; k < layer[0].node.length; k++) {//
                layer[0].weightObj[k].weight[j] += ALPHA * layer[0].delta[k] * data[j];
            }
        }
        e = Error(answer);
        return e;
    }
    public double sigmoid(double x){
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public double Error(double teach[]) {
        double e = 0.0;
        for (int i = 0; i < answer[0].length; i++) {
            //System.out.print(teach[i]);
            //System.out.print(layer[layerNum-1].node[i]);
            //System.out.println(Math.pow(teach[i] - layer[layerNum-1].node[i], 2.0));
            e += Math.pow(teach[i] - layer[layerNum-1].node[i], 2.0);
        }
        //System.out.println();
        e /= answer[0].length;
        //System.out.println(e);
        return e;
    }
}

class NetworkObject {
    int unitNum;
    double[] node;
    double[] delta;
    double[] e;
    eachWeight[] weightObj;

    NetworkObject(int inputUnitNum, int outputUnitNum) {
        this.unitNum=outputUnitNum;
        node = new double[unitNum];
        weightObj = new eachWeight[unitNum];
        delta = new double[unitNum];
        for (int i = 0; i < unitNum; i++) {
            weightObj[i] = new eachWeight(inputUnitNum);
        }
    }
}
class eachWeight{
    int inputUnitNum;
    double[] weight;
    eachWeight(int inputUnitNum) {
        this.inputUnitNum=inputUnitNum;
        weight = new double[inputUnitNum];
        Random rnd = new Random();
        for(int i=0;i<weight.length;i++){
            double num =rnd.nextDouble();
            weight[i]=(num-0.5)*0.01;
        }
    }
}
