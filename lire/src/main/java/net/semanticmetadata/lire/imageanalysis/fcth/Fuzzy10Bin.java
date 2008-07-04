package net.semanticmetadata.lire.imageanalysis.fcth;

public class Fuzzy10Bin {
    public boolean KeepPreviuesValues = false;


    protected static double[] HueMembershipValues = {0, 0, 5, 10,
            5, 10, 35, 50,
            35, 50, 70, 85,
            70, 85, 150, 165,
            150, 165, 195, 205,
            195, 205, 265, 280,
            265, 280, 315, 330,
            315, 330, 360, 360}; // Table Dimensions= Number Of Triangles X 4 (Start - Stop)

    protected static double[] SaturationMembershipValues = {0, 0, 10, 75,
            10, 75, 255, 255};

    protected static double[] ValueMembershipValues = {0, 0, 10, 75,
            10, 75, 180, 220,
            180, 220, 255, 255};

    //Vector fuzzy10BinRules = new Vector();
    public static FuzzyRules[] Fuzzy10BinRules = new FuzzyRules[48];

    public double[] Fuzzy10BinHisto = new double[10];
    public static double[] HueActivation = new double[8];
    public static double[] SaturationActivation = new double[2];
    public static double[] ValueActivation = new double[3];

    public static int[][] Fuzzy10BinRulesDefinition = {
            {0, 0, 0, 2},
            {0, 1, 0, 2},
            {0, 0, 2, 0},
            {0, 0, 1, 1},
            {1, 0, 0, 2},
            {1, 1, 0, 2},
            {1, 0, 2, 0},
            {1, 0, 1, 1},
            {2, 0, 0, 2},
            {2, 1, 0, 2},
            {2, 0, 2, 0},
            {2, 0, 1, 1},
            {3, 0, 0, 2},
            {3, 1, 0, 2},
            {3, 0, 2, 0},
            {3, 0, 1, 1},
            {4, 0, 0, 2},
            {4, 1, 0, 2},
            {4, 0, 2, 0},
            {4, 0, 1, 1},
            {5, 0, 0, 2},
            {5, 1, 0, 2},
            {5, 0, 2, 0},
            {5, 0, 1, 1},
            {6, 0, 0, 2},
            {6, 1, 0, 2},
            {6, 0, 2, 0},
            {6, 0, 1, 1},
            {7, 0, 0, 2},
            {7, 1, 0, 2},
            {7, 0, 2, 0},
            {7, 0, 1, 1},
            {0, 1, 1, 3},
            {0, 1, 2, 3},
            {1, 1, 1, 4},
            {1, 1, 2, 4},
            {2, 1, 1, 5},
            {2, 1, 2, 5},
            {3, 1, 1, 6},
            {3, 1, 2, 6},
            {4, 1, 1, 7},
            {4, 1, 2, 7},
            {5, 1, 1, 8},
            {5, 1, 2, 8},
            {6, 1, 1, 9},
            {6, 1, 2, 9},
            {7, 1, 1, 3},
            {7, 1, 2, 3}
    };  // 48 0 ������� ��� ������� ��� 4 �� 3 ������� ��� � ��� ������
    //

    public Fuzzy10Bin(boolean KeepPreviuesValues) {

        for (int R = 0; R < 48; R++) {
            //fuzzy10BinRules.addElement(new FuzzyRules());
            //FuzzyRules Fuzzy10BinRules = (FuzzyRules) fuzzy10BinRules.elementAt(R);
            Fuzzy10BinRules[R] = new FuzzyRules();
            Fuzzy10BinRules[R].Input1 = Fuzzy10BinRulesDefinition[R][0];
            Fuzzy10BinRules[R].Input2 = Fuzzy10BinRulesDefinition[R][1];
            Fuzzy10BinRules[R].Input3 = Fuzzy10BinRulesDefinition[R][2];
            Fuzzy10BinRules[R].Output = Fuzzy10BinRulesDefinition[R][3];

        }

        this.KeepPreviuesValues = KeepPreviuesValues;


    }


    private void FindMembershipValueForTriangles(double Input, double[] Triangles, double[] MembershipFunctionToSave) {
        int Temp = 0;

        for (int i = 0; i <= Triangles.length - 1; i += 4) {

            MembershipFunctionToSave[Temp] = 0;

            //�� ����� ������� ��� ������
            if (Input >= Triangles[i + 1] && Input <= +Triangles[i + 2]) {
                MembershipFunctionToSave[Temp] = 1;
            }

            //�� ����� ����� ��� ��������    
            if (Input >= Triangles[i] && Input < Triangles[i + 1]) {
                MembershipFunctionToSave[Temp] = (Input - Triangles[i]) / (Triangles[i + 1] - Triangles[i]);
            }

            //�� ����� �������� ��� ��������    

            if (Input > Triangles[i + 2] && Input <= Triangles[i + 3]) {
                MembershipFunctionToSave[Temp] = (Input - Triangles[i + 2]) / (Triangles[i + 2] - Triangles[i + 3]) + 1;
            }

            Temp += 1;
        }

    }


    private void LOM_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {
        int RuleActivation = -1;
        double LOM_MAXofMIN = 0;

        for (int i = 0; i < Rules.length; i++) {

            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {

                double Min = 0;
                Min = Math.min(Input1[Rules[i].Input1], Math.min(Input2[Rules[i].Input2], Input3[Rules[i].Input3]));

                if (Min > LOM_MAXofMIN) {
                    LOM_MAXofMIN = Min;
                    RuleActivation = Rules[i].Output;
                }

            }

        }


        ResultTable[RuleActivation]++;


    }


    private void MultiParticipate_Equal_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {

        int RuleActivation = -1;

        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {
                RuleActivation = Rules[i].Output;
                ResultTable[RuleActivation]++;

            }

        }
    }


    private void MultiParticipate_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {

        int RuleActivation = -1;

        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {
                RuleActivation = Rules[i].Output;
                double Min = 0;
                Min = Math.min(Input1[Rules[i].Input1], Math.min(Input2[Rules[i].Input2], Input3[Rules[i].Input3]));

                ResultTable[RuleActivation] += Min;

            }

        }
    }

    public double[] ApplyFilter(double Hue, double Saturation, double Value, int Method) {
        // Method   0 = LOM
        //          1 = Multi Equal Participate
        //          2 = Multi Participate

        if (KeepPreviuesValues == false) {
            for (int i = 0; i < 10; i++) {
                Fuzzy10BinHisto[i] = 0;
            }

        }

        FindMembershipValueForTriangles(Hue, HueMembershipValues, HueActivation);
        FindMembershipValueForTriangles(Saturation, SaturationMembershipValues, SaturationActivation);
        FindMembershipValueForTriangles(Value, ValueMembershipValues, ValueActivation);


        if (Method == 0)
            LOM_Defazzificator(Fuzzy10BinRules, HueActivation, SaturationActivation, ValueActivation, Fuzzy10BinHisto);
        if (Method == 1)
            MultiParticipate_Equal_Defazzificator(Fuzzy10BinRules, HueActivation, SaturationActivation, ValueActivation, Fuzzy10BinHisto);
        if (Method == 2)
            MultiParticipate_Defazzificator(Fuzzy10BinRules, HueActivation, SaturationActivation, ValueActivation, Fuzzy10BinHisto);

        return (Fuzzy10BinHisto);

    }
}