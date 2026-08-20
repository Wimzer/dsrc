package script.systems.crafting.droid.modules;

import script.*;
import script.library.*;

import java.util.Vector;

public class planetary_mining extends script.base_script
{
    public static final String[] PLANET_NAMES =
    {
        "Kashyyyk",
        "Mustafar"
    };
    public static final String[] PLANET_INTERNAL =
    {
        "kashyyyk_main",
        "mustafar"
    };
    public static final String[] KASHYYYK_SCENE_NAMES =
    {
        "Kashyyyk Main",
        "Dead Forest",
        "Etyyy",
        "Rryatt Trail"
    };
    public static final String[] KASHYYYK_SCENES =
    {
        "kashyyyk_main",
        "kashyyyk_dead_forest",
        "kashyyyk_hunting",
        "kashyyyk_rryatt_trail"
    };
    public static final float[][][] KASHYYYK_MAIN_SURVEY_REGIONS =
    {
        {
            { -656.647f, -38.718f }, { -696.029f, -31.968f }, { -812.005f, -83.995f }, { -848.729f, -241.738f }, { -941.691f, -279.995f }, { -995.997f, -236.012f }, { -950.752f, -88.052f }, { -775.951f, 52.037f },
            { -847.975f, 135.997f }, { -760.057f, 271.964f }, { -732.005f, 236.002f }, { -784.020f, 159.986f }, { -723.985f, 103.970f }, { -648.009f, 125.659f }, { -531.971f, 319.998f }, { -424.007f, 506.366f },
            { -419.998f, 731.999f }, { -295.987f, 892.000f }, { -204.013f, 923.960f }, { -155.990f, 863.979f }, { -124.009f, 879.962f }, { -116.014f, 860.039f }, { -156.048f, 837.313f }, { -216.020f, 752.849f },
            { -392.000f, 392.015f }, { -438.220f, 356.005f }, { -468.032f, 236.007f }, { -542.484f, 173.073f }, { -557.930f, 100.033f }, { -642.615f, 28.039f }, { -631.969f, -7.876f }
        },
        {
            { -485.448f, -94.360f }, { -394.827f, -136.028f }, { -311.818f, -143.385f }, { -280.005f, -162.301f }, { -229.708f, -151.114f }, { -224.611f, -88.066f }, { -184.001f, -52.013f }, { -144.024f, -48.165f },
            { -120.023f, -111.943f }, { -95.957f, -140.007f }, { -28.004f, -186.044f }, { 31.982f, -260.110f }, { 76.566f, -288.030f }, { 156.006f, -280.003f }, { 235.972f, -256.115f }, { 307.997f, -292.544f },
            { 299.984f, -343.970f }, { 316.000f, -424.004f }, { 412.034f, -448.350f }, { 495.939f, -492.002f }, { 583.979f, -516.000f }, { 591.994f, -568.011f }, { 655.977f, -580.007f }, { 712.007f, -548.024f },
            { 843.980f, -524.726f }, { 823.945f, -575.990f }, { 703.956f, -667.986f }, { 576.004f, -667.999f }, { 500.029f, -659.998f }, { 436.017f, -595.990f }, { 376.004f, -492.177f }, { 304.007f, -455.978f },
            { 258.011f, -436.000f }, { 208.011f, -459.989f }, { 184.009f, -376.009f }, { 224.006f, -343.986f }, { 178.912f, -299.980f }, { 80.018f, -315.993f }, { -55.996f, -211.998f }, { -131.995f, -171.995f },
            { -200.002f, -156.000f }, { -307.972f, -239.997f }, { -395.995f, -155.983f }, { -459.985f, -163.991f }, { -500.966f, -132.518f }
        },
        {
            { -336.438f, -153.522f }, { -307.999f, -96.007f }, { -271.997f, -76.006f }, { -263.988f, -12.005f }, { -311.999f, 84.009f }, { -418.596f, 128.014f }, { -573.153f, 108.620f }, { -548.041f, 170.734f },
            { -473.788f, 171.999f }, { -332.001f, 139.998f }, { -228.006f, 76.000f }, { -213.341f, 12.009f }, { -240.007f, -7.828f }, { -256.013f, -79.994f }, { -284.776f, -160.536f }
        },
        {
            { -700.000f, -179.000f }, { -687.000f, -88.000f }, { -639.000f, -96.000f }, { -663.000f, -32.000f }, { -646.000f, 7.000f }, { -509.000f, -19.000f }, { -412.000f, -14.000f }, { -524.000f, -191.000f }, { -636.000f, -167.000f }
        },
        {
            { 588.023f, 155.793f }, { 626.918f, 24.000f }, { 675.824f, -3.986f }, { 728.009f, -78.229f }, { 644.025f, -207.802f }, { 628.020f, -311.458f }, { 560.006f, -416.007f }, { 495.980f, -492.008f },
            { 428.012f, -587.941f }, { 587.251f, -667.984f }, { 787.945f, -607.928f }, { 847.994f, -444.011f }, { 755.987f, -248.044f }, { 775.998f, -24.015f }, { 775.994f, 72.038f }, { 852.635f, 72.000f },
            { 915.996f, 76.004f }, { 959.980f, 160.003f }, { 947.999f, 271.989f }, { 916.029f, 279.957f }, { 812.004f, 203.981f }, { 738.085f, 199.986f }, { 676.012f, 267.972f }, { 643.697f, 231.999f }
        },
        {
            { 651.996f, 228.386f }, { 700.004f, 291.985f }, { 791.304f, 395.863f }, { 844.020f, 455.998f }, { 899.984f, 511.997f }, { 907.999f, 480.055f }, { 831.932f, 339.735f }, { 740.004f, 260.002f },
            { 716.000f, 256.005f }, { 696.184f, 196.074f }
        },
        {
            { 647.631f, -240.295f }, { 578.186f, -262.381f }, { 535.986f, -268.009f }, { 514.807f, -252.081f }, { 499.986f, -212.023f }, { 454.143f, -176.484f }, { 383.703f, -187.833f }, { 315.997f, -124.098f },
            { 276.001f, -128.007f }, { 243.868f, -128.016f }, { 177.886f, -92.421f }, { 159.953f, -117.951f }, { 204.083f, -160.001f }, { 204.008f, -183.980f }, { 236.011f, -207.965f }, { 288.023f, -243.985f },
            { 359.983f, -235.985f }, { 415.967f, -199.994f }, { 448.013f, -191.996f }, { 480.000f, -211.960f }, { 492.022f, -251.999f }, { 528.441f, -283.997f }, { 583.998f, -271.977f }, { 632.069f, -265.917f },
            { 656.433f, -269.521f }
        },
        {
            { 692.004f, 279.992f }, { 604.002f, 176.998f }, { 492.031f, 47.989f }, { 481.547f, -40.013f }, { 352.436f, 19.972f }, { 245.836f, -12.000f }, { 143.204f, -24.019f }, { 28.182f, 30.823f },
            { -32.096f, 81.729f }, { -151.827f, 103.997f }, { -228.000f, 75.942f }, { -336.005f, 143.986f }, { -455.978f, 175.994f }, { -540.002f, 162.545f }, { -565.291f, 92.015f }, { -464.018f, 124.021f },
            { -363.962f, 116.049f }, { -299.924f, 64.035f }, { -191.990f, 8.066f }, { 51.980f, -67.981f }, { 160.016f, -82.768f }, { 291.998f, -63.975f }, { 445.214f, -103.990f }, { 563.998f, -83.982f },
            { 599.942f, 12.000f }, { 626.917f, 96.190f }, { 711.979f, 231.998f }, { 735.098f, 277.401f }
        },
        {
            { 809.452f, 378.389f }, { 784.013f, 428.019f }, { 740.013f, 472.004f }, { 724.023f, 554.626f }, { 696.003f, 616.006f }, { 464.018f, 644.027f }, { 372.025f, 680.007f }, { 336.158f, 731.996f },
            { 384.061f, 755.986f }, { 392.000f, 803.992f }, { 408.026f, 863.994f }, { 380.030f, 928.020f }, { 396.013f, 975.020f }, { 435.990f, 975.579f }, { 435.972f, 923.998f }, { 455.998f, 848.073f },
            { 459.966f, 792.005f }, { 431.968f, 742.390f }, { 443.996f, 707.942f }, { 519.999f, 671.998f }, { 540.146f, 675.970f }, { 552.021f, 743.993f }, { 628.001f, 827.990f }, { 712.000f, 879.995f },
            { 795.989f, 875.998f }, { 851.950f, 843.977f }, { 863.000f, 727.000f }, { 855.992f, 664.004f }, { 795.969f, 564.968f }, { 783.995f, 533.073f }, { 823.976f, 452.322f }, { 862.004f, 413.292f }
        },
        {
            { 435.971f, 716.000f }, { 431.964f, 646.483f }, { 435.988f, 584.032f }, { 439.972f, 484.003f }, { 391.964f, 452.004f }, { 407.994f, 367.970f }, { 467.982f, 303.989f }, { 471.999f, 267.995f },
            { 488.001f, 295.978f }, { 500.033f, 331.983f }, { 516.028f, 368.026f }, { 484.047f, 416.001f }, { 480.070f, 463.979f }, { 496.071f, 479.963f }, { 531.997f, 511.985f }, { 599.966f, 475.995f },
            { 583.997f, 416.061f }, { 547.993f, 388.011f }, { 543.941f, 351.979f }, { 567.994f, 252.016f }, { 535.997f, 204.012f }, { 471.997f, 212.018f }, { 436.074f, 218.561f }, { 432.000f, 288.009f },
            { 379.997f, 320.031f }, { 351.986f, 292.028f }, { 327.979f, 247.973f }, { 351.979f, 202.470f }, { 347.925f, 159.946f }, { 416.016f, 147.999f }, { 471.940f, 143.325f }, { 451.981f, 76.091f },
            { 354.950f, 40.020f }, { 301.463f, 16.023f }, { 247.352f, -12.658f }, { 240.563f, -37.964f }, { 165.995f, -41.766f }, { 184.005f, -12.051f }, { 212.019f, 48.459f }, { 190.155f, 88.007f },
            { 100.041f, 136.038f }, { 76.003f, 170.886f }, { 120.032f, 235.981f }, { 159.982f, 223.980f }, { 207.975f, 131.984f }, { 268.010f, 139.992f }, { 324.001f, 178.596f }, { 316.008f, 220.114f },
            { 296.014f, 228.032f }, { 288.029f, 267.970f }, { 312.037f, 304.943f }, { 280.025f, 455.983f }, { 308.012f, 495.977f }, { 372.001f, 515.997f }, { 380.067f, 675.115f }, { 376.314f, 751.991f }
        },
        {
            { 367.102f, 44.028f }, { 459.991f, 88.002f }, { 471.985f, 155.960f }, { 471.977f, 218.887f }, { 504.005f, 204.056f }, { 535.995f, 204.005f }, { 498.740f, 299.987f }, { 471.533f, 262.561f },
            { 424.032f, 259.980f }, { 432.017f, 163.376f }, { 408.277f, 143.975f }, { 348.000f, 160.000f }
        },
        {
            { 230.910f, -4.594f }, { 247.949f, -92.000f }, { 175.999f, -155.978f }, { 99.989f, -183.983f }, { 60.038f, -163.934f }, { -24.036f, -179.999f }, { -77.986f, -144.046f }, { 0.945f, -112.026f },
            { 132.010f, -100.007f }, { 160.049f, -86.605f }, { 200.012f, 11.956f }
        },
        {
            { 372.019f, 751.956f }, { 299.159f, 731.994f }, { 231.995f, 783.999f }, { 127.959f, 839.969f }, { 72.008f, 807.971f }, { 12.066f, 807.997f }, { -40.002f, 767.989f }, { -56.040f, 785.513f },
            { -50.919f, 840.000f }, { -75.978f, 843.989f }, { -95.944f, 796.000f }, { -187.936f, 747.975f }, { -216.082f, 743.214f }, { -270.206f, 754.046f }, { -230.423f, 676.186f }, { -196.001f, 708.030f },
            { -75.927f, 724.903f }, { -103.972f, 609.255f }, { -139.951f, 563.993f }, { -199.917f, 499.832f }, { -292.080f, 478.480f }, { -362.138f, 552.162f }, { -443.308f, 393.063f }, { -392.017f, 392.087f },
            { -248.014f, 412.020f }, { -189.177f, 448.000f }, { -168.018f, 444.010f }, { -115.994f, 440.035f }, { -92.046f, 459.992f }, { -116.011f, 479.998f }, { -97.069f, 532.055f }, { -56.022f, 624.049f },
            { -12.010f, 703.998f }, { -0.007f, 752.019f }, { 36.005f, 768.116f }, { 112.141f, 732.007f }, { 176.778f, 724.012f }, { 196.004f, 681.585f }, { 148.059f, 647.987f }, { 112.005f, 579.990f },
            { 109.095f, 508.006f }, { 152.002f, 468.000f }, { 228.000f, 476.022f }, { 249.917f, 488.015f }, { 296.150f, 435.801f }, { 318.179f, 479.319f }, { 279.935f, 487.957f }, { 231.984f, 583.996f },
            { 219.928f, 683.994f }, { 227.998f, 724.000f }, { 303.985f, 688.001f }, { 372.013f, 680.001f }
        },
        {
            { 76.030f, 535.915f }, { 19.946f, 503.994f }, { -43.998f, 487.977f }, { -55.934f, 455.987f }, { -80.828f, 447.891f }, { -103.997f, 420.001f }, { -115.915f, 199.975f }, { -143.995f, 143.949f },
            { -143.798f, 70.669f }, { -21.051f, 67.941f }, { -89.942f, 150.823f }, { -88.001f, 224.004f }, { -72.016f, 302.818f }, { -72.012f, 374.166f }, { -24.049f, 480.014f }, { 39.991f, 484.119f },
            { 64.000f, 512.037f }, { 98.439f, 512.019f }
        },
        {
            { -67.721f, 435.782f }, { -116.012f, 474.095f }, { -157.103f, 520.394f }, { -189.537f, 448.084f }, { -132.000f, 448.001f }, { -107.992f, 428.009f }, { -99.983f, 389.915f }
        },
        {
            { 97.218f, 557.386f }, { 52.024f, 519.992f }, { 59.998f, 508.010f }, { 117.924f, 507.231f }
        }
    };
    public static final float[][][] KASHYYYK_MAIN_SURVEY_EXCLUSION_REGIONS =
    {
        {
            { 664.010f, -455.937f }, { 631.964f, -464.000f }, { 583.988f, -515.980f }, { 591.999f, -568.016f }, { 688.013f, -576.013f }, { 712.003f, -507.990f }, { 692.021f, -475.998f }
        }
    };
    public static final float[][][] KASHYYYK_RRYATT_TRAIL_SURVEY_REGIONS =
    {
        { { 514.264f, 3423.964f }, { 551.977f, 3419.999f }, { 607.977f, 3451.999f }, { 615.997f, 3437.192f }, { 599.987f, 3418.093f }, { 605.309f, 3389.365f }, { 636.608f, 3383.990f }, { 635.969f, 3368.347f }, { 591.999f, 3351.498f }, { 603.999f, 3316.002f }, { 568.012f, 3336.010f }, { 520.005f, 3353.958f } },
        { { 985.217f, 3399.984f }, { 999.982f, 3388.004f }, { 1088.000f, 3279.294f }, { 1074.145f, 3256.011f }, { 936.027f, 3308.024f }, { 928.550f, 3345.847f }, { 968.391f, 3391.967f } },
        { { 955.991f, 3464.009f }, { 944.638f, 3500.986f }, { 911.998f, 3537.449f }, { 970.954f, 3600.490f }, { 934.515f, 3655.966f }, { 1019.959f, 3729.344f }, { 1015.734f, 3830.356f }, { 992.028f, 3831.525f }, { 920.014f, 3771.992f }, { 916.071f, 3728.009f }, { 956.015f, 3707.756f }, { 946.489f, 3691.769f }, { 886.310f, 3712.000f }, { 868.003f, 3705.290f }, { 812.006f, 3667.646f }, { 812.003f, 3640.004f }, { 848.003f, 3595.991f }, { 824.001f, 3494.611f }, { 816.013f, 3472.005f }, { 901.500f, 3453.936f } },
        { { 996.265f, 3896.007f }, { 968.012f, 3963.758f }, { 997.474f, 4011.952f }, { 1046.255f, 4007.898f }, { 1087.403f, 3964.250f }, { 1055.991f, 3936.018f }, { 1019.974f, 3892.002f } },
        { { 1268.463f, 4064.075f }, { 1244.013f, 4036.003f }, { 1208.014f, 4084.001f }, { 1168.004f, 4128.156f }, { 1185.652f, 4151.997f }, { 1239.990f, 4123.193f }, { 1275.513f, 4091.237f } },
        { { 1107.983f, 4180.005f }, { 1072.003f, 4168.006f }, { 961.545f, 4188.000f }, { 868.009f, 4328.001f }, { 812.010f, 4383.443f }, { 807.841f, 4469.697f }, { 788.001f, 4472.002f }, { 780.006f, 4514.819f }, { 819.991f, 4520.000f }, { 835.994f, 4436.004f }, { 843.977f, 4375.981f }, { 940.057f, 4401.294f }, { 1024.860f, 4362.722f }, { 1079.976f, 4358.047f }, { 1127.396f, 4312.375f }, { 1131.300f, 4258.754f }, { 1108.018f, 4199.997f } },
        { { 1240.867f, 4279.963f }, { 1232.012f, 4375.975f }, { 1295.986f, 4431.977f }, { 1489.784f, 4491.970f }, { 1547.996f, 4495.994f }, { 1544.209f, 4440.096f }, { 1479.569f, 4447.927f }, { 1415.999f, 4378.527f }, { 1414.364f, 4355.027f }, { 1491.723f, 4329.046f }, { 1535.214f, 4270.481f }, { 1490.264f, 4240.014f }, { 1339.999f, 4264.009f }, { 1208.020f, 4260.006f }, { 1207.012f, 4275.970f } },
        { { 1646.387f, 4203.992f }, { 1681.808f, 4235.953f }, { 1703.998f, 4193.184f }, { 1611.978f, 4074.474f }, { 1628.336f, 4003.939f }, { 1690.234f, 3985.593f }, { 1653.904f, 3956.428f }, { 1593.309f, 3996.621f }, { 1581.885f, 4067.949f }, { 1662.630f, 4156.616f }, { 1632.009f, 4184.008f } }
    };
    public static final float[][][] KASHYYYK_DEAD_FOREST_SURVEY_REGIONS =
    {
        { { 76.841f, -471.982f }, { 17.836f, -371.741f }, { -55.962f, -336.292f }, { -65.994f, -307.427f }, { -141.115f, -286.655f }, { -187.997f, -275.997f }, { -187.986f, -237.799f }, { -223.985f, -199.651f }, { -273.220f, -76.694f }, { -235.085f, -72.264f }, { -196.011f, -204.001f }, { -123.242f, -256.826f }, { -53.592f, -291.661f }, { -10.361f, -280.592f }, { 27.961f, -304.782f }, { 57.070f, -356.019f }, { 123.304f, -325.258f }, { 128.509f, -363.999f }, { 79.985f, -383.901f }, { 99.528f, -473.494f } },
        { { 119.654f, -357.717f }, { 245.118f, -487.994f }, { 341.633f, -438.447f }, { 423.559f, -483.991f }, { 475.997f, -435.104f }, { 479.958f, -310.296f }, { 455.964f, -286.609f }, { 483.996f, -249.631f }, { 471.997f, -184.230f }, { 455.981f, -108.001f }, { 367.976f, -89.451f }, { 285.344f, 24.167f }, { 399.987f, 173.989f }, { 399.683f, 211.060f }, { 325.414f, 269.859f }, { 319.993f, 313.565f }, { 166.117f, 323.012f }, { 79.121f, 267.614f }, { 100.012f, 168.002f }, { 72.008f, -36.000f }, { 140.009f, -171.986f }, { 119.089f, -336.895f } },
        { { -145.809f, -268.414f }, { -68.606f, -191.998f }, { -102.107f, -152.017f }, { -148.919f, -91.506f }, { -187.989f, -94.803f }, { -203.997f, -129.828f }, { -188.000f, -174.895f }, { -167.610f, -183.989f }, { -119.958f, -171.997f }, { -103.981f, -196.009f }, { -165.701f, -241.462f } },
        { { -265.196f, -185.821f }, { -276.560f, -157.229f }, { -307.999f, -172.011f }, { -315.998f, -189.902f }, { -283.269f, -203.238f } },
        { { -264.007f, -391.993f }, { -295.995f, -356.024f }, { -315.990f, -391.994f }, { -335.388f, -408.919f }, { -360.001f, -383.001f }, { -368.006f, -345.062f }, { -415.947f, -371.992f }, { -391.996f, -418.831f }, { -308.022f, -423.999f } },
        { { -364.011f, -370.686f }, { -372.000f, -252.026f }, { -376.001f, -179.994f }, { -312.018f, -107.986f }, { -238.950f, -83.010f }, { -124.026f, -59.976f }, { 7.976f, -15.952f }, { 173.431f, 5.457f }, { 95.033f, 143.999f }, { -4.270f, 111.993f }, { -107.088f, 73.924f }, { -197.431f, 100.989f }, { -255.985f, 163.994f }, { -367.990f, 71.990f }, { -459.964f, -199.994f }, { -439.989f, -326.859f }, { -391.995f, -418.831f }, { -355.995f, -419.979f } },
        { { -382.798f, 27.976f }, { -353.911f, 106.269f }, { -245.444f, 165.131f }, { -153.528f, 195.779f }, { -123.975f, 259.998f }, { -103.308f, 228.573f }, { 36.007f, 259.997f }, { 159.895f, 309.699f }, { 114.085f, 206.988f }, { 46.643f, 198.482f }, { -47.997f, 176.841f }, { -118.174f, 62.428f }, { -258.736f, -60.730f }, { -404.839f, -125.678f }, { -395.976f, -0.010f } },
        { { -354.774f, 84.193f }, { -363.977f, 161.905f }, { -355.999f, 199.971f }, { -303.984f, 275.541f }, { -307.997f, 300.006f }, { -327.993f, 333.357f }, { -310.754f, 387.999f }, { -275.032f, 415.948f }, { -226.094f, 415.395f }, { -159.998f, 459.987f }, { -112.001f, 400.720f }, { -112.003f, 289.643f }, { -175.597f, 316.006f }, { -212.776f, 260.029f }, { -287.840f, 244.222f }, { -316.217f, 228.013f }, { -340.055f, 192.041f }, { -336.074f, 137.738f }, { -324.444f, 113.680f } }
    };
    public static final float[][][] KASHYYYK_HUNTING_SURVEY_REGIONS =
    {
        { { 663.774f, 683.964f }, { 572.027f, 604.401f }, { 560.897f, 575.982f }, { 533.957f, 571.992f }, { 480.129f, 527.996f }, { 443.983f, 595.993f }, { 325.079f, 643.998f }, { 243.990f, 632.000f }, { 175.982f, 627.981f }, { 156.020f, 610.950f }, { 138.305f, 595.990f }, { 104.012f, 607.991f }, { 80.005f, 556.011f }, { 112.017f, 503.994f }, { 88.021f, 451.999f }, { 72.011f, 425.144f }, { 118.475f, 372.003f }, { 151.988f, 380.011f }, { 163.998f, 404.002f }, { 206.876f, 436.009f }, { 264.001f, 404.023f }, { 355.999f, 408.005f }, { 392.246f, 436.001f }, { 519.999f, 456.036f }, { 599.998f, 504.012f }, { 607.579f, 554.736f }, { 631.775f, 616.279f }, { 671.999f, 665.856f } },
        { { 82.090f, 427.248f }, { -13.151f, 382.413f }, { -44.004f, 389.265f }, { -52.035f, 431.985f }, { -97.912f, 447.998f }, { -128.015f, 485.012f }, { -128.000f, 543.972f }, { -148.005f, 554.119f }, { -136.027f, 572.453f }, { -98.616f, 588.001f }, { -75.975f, 568.080f }, { -99.979f, 555.989f }, { -95.998f, 532.011f }, { -56.010f, 532.005f }, { -40.002f, 560.002f }, { -80.008f, 615.968f }, { -76.000f, 651.996f }, { -118.950f, 679.990f }, { -236.012f, 695.998f }, { -315.997f, 691.982f }, { -318.035f, 635.995f }, { -364.011f, 655.993f }, { -376.004f, 729.747f }, { -352.023f, 791.992f }, { -392.011f, 836.019f }, { -364.000f, 875.999f }, { -435.982f, 903.986f }, { -504.611f, 867.998f }, { -512.001f, 915.981f }, { -607.077f, 947.981f }, { -659.992f, 915.992f }, { -655.965f, 844.049f }, { -592.004f, 828.006f }, { -568.005f, 848.011f }, { -543.995f, 852.020f }, { -543.994f, 831.995f }, { -579.993f, 787.994f }, { -671.987f, 811.991f }, { -687.944f, 780.002f }, { -647.986f, 740.001f }, { -584.776f, 648.001f }, { -463.931f, 482.905f }, { -543.981f, 193.431f }, { -439.992f, 36.011f }, { -407.980f, -87.969f }, { -180.002f, -103.956f }, { -155.017f, -67.998f }, { 146.239f, 104.027f }, { 112.777f, 267.966f }, { 35.983f, 309.786f }, { 133.369f, 384.381f } },
        { { 168.971f, 164.249f }, { 215.535f, 124.722f }, { 295.176f, 176.280f }, { 435.990f, 20.753f }, { 412.865f, -38.870f }, { 516.220f, -81.225f }, { 668.011f, 113.528f }, { 703.261f, 113.909f }, { 753.204f, 146.063f }, { 759.532f, 183.664f }, { 801.894f, 253.370f }, { 868.111f, 273.157f }, { 877.904f, 297.657f }, { 861.362f, 371.976f }, { 899.998f, 415.983f }, { 896.042f, 334.221f }, { 927.617f, 331.562f }, { 960.003f, 438.114f }, { 940.700f, 507.522f }, { 1028.700f, 599.960f }, { 1100.180f, 615.993f }, { 1139.970f, 568.022f }, { 1089.146f, 524.641f }, { 1047.984f, 475.572f }, { 1065.917f, 431.899f }, { 1058.168f, 383.270f }, { 1095.993f, 395.987f }, { 963.646f, 274.958f }, { 840.942f, 135.732f }, { 1017.792f, 225.218f }, { 1104.200f, 202.748f }, { 1107.984f, 133.702f }, { 932.012f, 112.012f }, { 915.999f, 72.001f }, { 743.084f, -25.110f }, { 776.680f, -71.753f }, { 819.970f, -93.903f }, { 749.285f, -89.447f }, { 705.113f, -121.665f }, { 743.990f, -144.000f }, { 728.265f, -159.997f }, { 678.409f, -156.949f }, { 573.849f, -197.077f }, { 522.241f, -171.986f }, { 504.316f, -176.184f }, { 463.975f, -262.957f }, { 413.561f, -320.627f }, { 371.820f, -338.739f }, { 365.658f, -365.955f }, { 330.613f, -402.550f }, { 232.802f, -306.757f }, { 235.993f, -280.018f }, { 259.279f, -240.121f }, { 211.871f, -141.789f }, { 192.641f, -134.069f }, { 194.955f, -91.518f }, { 131.993f, -63.012f }, { 38.225f, -66.324f }, { 25.975f, -29.205f }, { 34.977f, 5.603f }, { 166.223f, 73.202f }, { 115.574f, 94.919f } },
        { { 418.355f, -299.350f }, { 422.033f, -327.646f }, { 468.169f, -356.326f }, { 603.986f, -340.001f }, { 692.005f, -329.419f }, { 701.397f, -316.084f }, { 734.657f, -346.059f }, { 792.849f, -336.046f }, { 816.083f, -316.031f }, { 875.132f, -336.001f }, { 960.000f, -350.617f }, { 960.000f, -348.142f }, { 1056.536f, -356.478f }, { 1175.982f, -363.677f }, { 1191.220f, -388.001f }, { 1247.986f, -404.005f }, { 1263.996f, -451.482f }, { 1291.997f, -473.786f }, { 1267.994f, -539.989f }, { 1219.992f, -595.980f }, { 1183.986f, -603.979f }, { 1117.113f, -647.985f }, { 1032.005f, -655.989f }, { 965.206f, -595.299f }, { 896.010f, -531.995f }, { 791.449f, -499.975f }, { 795.996f, -583.998f }, { 729.987f, -627.216f }, { 688.025f, -643.995f }, { 672.000f, -596.021f }, { 689.869f, -581.130f }, { 739.456f, -581.855f }, { 768.562f, -565.739f }, { 767.754f, -527.424f }, { 651.510f, -489.634f }, { 647.995f, -539.996f }, { 571.288f, -580.432f }, { 534.663f, -615.998f }, { 500.004f, -548.053f }, { 485.017f, -532.304f }, { 372.003f, -507.350f }, { 352.125f, -444.415f }, { 315.211f, -430.033f }, { 296.004f, -451.991f }, { 285.095f, -436.951f }, { 211.650f, -435.683f }, { 187.171f, -430.886f }, { 101.564f, -352.032f }, { 148.001f, -316.001f }, { 176.809f, -338.233f }, { 226.178f, -357.329f }, { 233.241f, -293.184f }, { 258.263f, -258.661f }, { 343.196f, -247.435f } }
    };
    public static final String[] RESOURCE_CLASSES =
    {
        "mineral",
        "chemical",
        "gas",
        "flora_resources",
        "water",
        "energy"
    };
    public static final String[] RESOURCE_CLASS_NAMES =
    {
        "Minerals",
        "Chemicals",
        "Gases",
        "Flora",
        "Water",
        "Energy"
    };
    public static final String VAR_PLANET = "planetary_mining.planet";
    public static final String VAR_SURVEY_LOCATION = "planetary_mining.survey_location";
    public static final String VAR_RESOURCE_CLASS = "planetary_mining.resource_class";
    public static final String VAR_SELECTED_RESOURCE_CLASS = "planetary_mining.selected_resource_class";
    public static final String VAR_SURVEY_CANDIDATES = "planetary_mining.survey_candidates";
    public static final String VAR_SURVEY_INDEX = "planetary_mining.survey_index";
    public static final String VAR_SURVEY_RESULT_TYPES = "planetary_mining.survey_result_types";
    public static final String VAR_SURVEY_RESULT_DENSITIES = "planetary_mining.survey_result_densities";
    public static final String VAR_SURVEY_RESULT_LOCATIONS = "planetary_mining.survey_result_locations";
    public static final String VAR_SURVEY_BEST_DENSITY = "planetary_mining.survey_best_density";
    public static final String VAR_SURVEY_BEST_LOCATION = "planetary_mining.survey_best_location";
    public static final String VAR_RESOURCE_TYPE = "planetary_mining.resource_type";
    public static final String VAR_SURVEY_SELECTED = "planetary_mining.survey_selected";
    public static final String VAR_ACCOUNT_RESERVATION_PENDING = "planetary_mining.account_reservation_pending";
    public static final String VAR_FLOW_PLAYER = "planetary_mining.flow.player";
    public static final String VAR_FLOW_ITEM = "planetary_mining.flow.item";
    public static final String VAR_FLOW_PID = "planetary_mining.flow.pid";
    public static final String VAR_LAUNCH_COUNTDOWN = "planetary_mining.launch_countdown";
    public static final String VAR_PENDING_LAUNCH_ITEM = "planetary_mining.pending_launch_item";
    public static final String VAR_PENDING_LAUNCH_OPERATION = "planetary_mining.pending_launch_operation";
    public static final String VAR_JOB_SEQUENCE = "planetary_mining.job_sequence";
    public static final String VAR_ACTIVE_JOB_SEQUENCE = "planetary_mining.active_job_sequence";
    public static final String VAR_PENDING_RELEASE_SEQUENCES = "planetary_mining.pending_release_sequences";
    public static final String VAR_RELEASE_IN_FLIGHT = "planetary_mining.release_in_flight";
    public static final String VAR_LAUNCH_AMOUNT = "planetary_mining.launch_amount";
    public static final String VAR_LAUNCH_JOB_SEQUENCE = "planetary_mining.launch_job_sequence";
    public static final String PID_NAME = "planetaryMiningDroid";
    public static final String DISPLAY_NAME = "Interplanetary Mining Droid";
    public static final String ATTRIBUTE_BASE = craftinglib.COMPONENT_ATTRIBUTE_OBJVAR_NAME + ".";
    public static final String LEGACY_STATIC_ATTRIBUTE_BASE = "crafting.component_attribute.";
    public static final String ATTRIBUTE_EXTRACTION_RATE = ATTRIBUTE_BASE + "extractRate";
    public static final float MIN_ACTIVE_DENSITY = 0.0001f;
    public static final float MUSTAFAR_DISPLAY_MIN_COORDINATE = -4000.0f;
    public static final float MUSTAFAR_DISPLAY_MAX_COORDINATE = 4000.0f;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_X = 1294.0f;
    public static final float RRYATT_TRAIL_WORLD_OFFSET_Z = 3880.0f;
    public static final float SURVEY_GRID_RADIUS = 32.0f;
    public static final float KASHYYYK_MAIN_SURVEY_EDGE_OUTSET = 5.0f;
    public static final int MAX_COORDINATE_INPUT_LENGTH = 32;

    public int OnInitialize(obj_id self) throws InterruptedException
    {
        setName(self, DISPLAY_NAME);
        return SCRIPT_CONTINUE;
    }

    public int OnDestroy(obj_id self) throws InterruptedException
    {
        cleanScriptVars(self);
        return SCRIPT_CONTINUE;
    }

    public int OnAboutToBeTransferred(obj_id self, obj_id destination, obj_id transferer) throws InterruptedException
    {
        return utils.hasScriptVar(self, VAR_FLOW_PLAYER) ? SCRIPT_OVERRIDE : SCRIPT_CONTINUE;
    }

    public int OnObjectMenuRequest(obj_id self, obj_id player, menu_info mi) throws InterruptedException
    {
        menu_info_data mid = mi.getMenuItemByType(menu_info_types.ITEM_USE);
        if (mid == null)
        {
            int menu = mi.addRootMenu(menu_info_types.ITEM_USE, new string_id("", ""));
            mid = mi.getMenuItemById(menu);
        }
        mid.setServerNotify(true);
        return SCRIPT_CONTINUE;
    }

    public int OnObjectMenuSelect(obj_id self, obj_id player, int item) throws InterruptedException
    {
        if (item != menu_info_types.ITEM_USE)
        {
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player))
        {
            return SCRIPT_CONTINUE;
        }
        if (!utils.isProfession(player, utils.TRADER))
        {
            sendSystemMessage(player, "The interface for this droid is too complex for you to interact with.", null);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            return SCRIPT_CONTINUE;
        }
        if (getTopMostContainer(player) != player)
        {
            sendSystemMessage(player, "You must be outdoors to launch this droid.", null);
            return SCRIPT_OVERRIDE;
        }
        if (!beginFlow(self, player))
        {
            sendSystemMessage(player, "An Interplanetary Mining Droid interface is already active.", null);
            return SCRIPT_CONTINUE;
        }
        trackSui(self, player, sui.listbox(self, player, "Select the planet to mine.", sui.OK_CANCEL, DISPLAY_NAME, PLANET_NAMES, "handleMiningPlanetSelection"));
        return SCRIPT_CONTINUE;
    }

    public int handleMiningPlanetSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int index = sui.getListboxSelectedRow(params);
        if (!isEligibleMiningDroidUser(self, player) || index < 0 || index >= PLANET_INTERNAL.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (index == 0)
        {
            trackSui(self, player, sui.listbox(self, player, "Select the Kashyyyk scene to survey.", sui.OK_CANCEL, DISPLAY_NAME, KASHYYYK_SCENE_NAMES, "handleMiningKashyyykSceneSelection"));
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_PLANET, PLANET_INTERNAL[index]);
        promptForMiningCoordinates(self, player);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningKashyyykSceneSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int index = sui.getListboxSelectedRow(params);
        if (index < 0 || index >= KASHYYYK_SCENES.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_PLANET, KASHYYYK_SCENES[index]);
        promptForMiningCoordinates(self, player);
        return SCRIPT_CONTINUE;
    }

    public void promptForMiningCoordinates(obj_id self, obj_id player) throws InterruptedException
    {
        trackSui(self, player, sui.inputbox(self, player, "Enter X, Z or X, Y, Z. You may paste /waypoint coordinates.", sui.OK_CANCEL, DISPLAY_NAME, sui.INPUT_NORMAL, null, "handleMiningCoordinates", null));
    }

    public int handleMiningCoordinates(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (params == null || params.isEmpty() || sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String coordinateInput = sui.getInputBoxText(params);
        if (coordinateInput == null)
        {
            sendSystemMessage(player, "Enter coordinates to survey.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        coordinateInput = coordinateInput.trim().replaceFirst("(?i)^/waypoint(?:[,\\s]+)", "");
        if (coordinateInput.length() > MAX_COORDINATE_INPUT_LENGTH)
        {
            sendSystemMessage(player, "Coordinate input is too long.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        String[] coordinates = coordinateInput.split("[,\\s]+");
        if (coordinates.length != 2 && coordinates.length != 3)
        {
            sendSystemMessage(player, "Enter X, Z or X, Y, Z coordinates.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        float[] values = new float[coordinates.length];
        try
        {
            for (int i = 0; i < coordinates.length; ++i)
            {
                values[i] = Float.parseFloat(coordinates[i]);
            }
        }
        catch (NumberFormatException exception)
        {
            sendSystemMessage(player, "Coordinates must be numbers.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        for (float value : values)
        {
            if (Float.isNaN(value) || Float.isInfinite(value))
            {
                sendSystemMessage(player, "Coordinates must be finite numbers.", null);
                promptForMiningCoordinates(self, player);
                return SCRIPT_CONTINUE;
            }
        }
        float x = values[0];
        float z = values[values.length - 1];
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        if (planet.equals("mustafar") && (x < MUSTAFAR_DISPLAY_MIN_COORDINATE + SURVEY_GRID_RADIUS || x > MUSTAFAR_DISPLAY_MAX_COORDINATE - SURVEY_GRID_RADIUS || z < MUSTAFAR_DISPLAY_MIN_COORDINATE + SURVEY_GRID_RADIUS || z > MUSTAFAR_DISPLAY_MAX_COORDINATE - SURVEY_GRID_RADIUS))
        {
            sendSystemMessage(player, "Those coordinates are outside the surveyable area.", null);
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        float worldX = x;
        float worldZ = z;
        if (planet.equals("mustafar"))
        {
            worldX = x - 2880.0f;
            worldZ = z + 2976.0f;
        }
        else if (planet.equals("kashyyyk_rryatt_trail"))
        {
            worldX = x + RRYATT_TRAIL_WORLD_OFFSET_X;
            worldZ = z + RRYATT_TRAIL_WORLD_OFFSET_Z;
        }
        location surveyLocation = new location(worldX, 0, worldZ, planet);
        if (!isAllowedSurveyGrid(planet, surveyLocation))
        {
            if (planet.equals("kashyyyk_rryatt_trail"))
            {
                sendSystemMessage(player, "That area of the trail is too dangerous for your droid.", null);
            }
            else if (planet.equals("kashyyyk_dead_forest"))
            {
                sendSystemMessage(player, "The Sayormi's dark magic prevents your droid from surveying there.", null);
            }
            else
            {
                sendSystemMessage(player, "Those coordinates are outside the surveyable area.", null);
            }
            promptForMiningCoordinates(self, player);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SURVEY_LOCATION, surveyLocation);
        Vector availableClasses = new Vector();
        Vector availableClassNames = new Vector();
        for (int i = 0; i < RESOURCE_CLASSES.length; ++i)
        {
            obj_id[] availableResourceTypes = getAvailablePmdResourceTypes(surveyLocation, RESOURCE_CLASSES[i]);
            if (availableResourceTypes != null && availableResourceTypes.length > 0)
            {
                availableClasses.add(RESOURCE_CLASSES[i]);
                availableClassNames.add(RESOURCE_CLASS_NAMES[i]);
            }
        }
        if (availableClasses.size() == 0)
        {
            sendSystemMessage(player, "No resources are currently available in the selected area.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String[] resourceClasses = new String[availableClasses.size()];
        String[] resourceClassNames = new String[availableClassNames.size()];
        availableClasses.toArray(resourceClasses);
        availableClassNames.toArray(resourceClassNames);
        utils.setScriptVar(self, VAR_RESOURCE_CLASS, resourceClasses);
        trackSui(self, player, sui.listbox(self, player, "Select a survey type.", sui.OK_CANCEL, DISPLAY_NAME, resourceClassNames, "handleMiningClassSelection"));
        return SCRIPT_CONTINUE;
    }

    public int handleMiningClassSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (!isListSelectionValid(params))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!isEligibleMiningDroidUser(self, player))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String[] availableClasses = utils.getStringArrayScriptVar(self, VAR_RESOURCE_CLASS);
        int index = sui.getListboxSelectedRow(params);
        if (availableClasses == null || index < 0 || index >= availableClasses.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        String resourceClass = availableClasses[index];
        if (!isAllowedSurveyGrid(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_SELECTED_RESOURCE_CLASS, resourceClass);
        startPmdSurveys(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public int handleMiningResourceConfirm(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
            utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
            utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
            utils.removeScriptVar(self, VAR_SURVEY_SELECTED);
            showPmdSurveyResults(self, player);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!utils.hasScriptVar(self, VAR_SURVEY_SELECTED) || !isEligibleMiningDroidUser(self, player) || !isAllowedSurveyGrid(planet, surveyLocation))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!utils.isNestedWithin(self, player) || !isSelectedResourceAvailable(self, resourceType))
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN) || utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, 3);
        sendSystemMessage(player, "3", null);
        dictionary countdown = new dictionary();
        countdown.put("step", 2);
        messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidLaunchCountdown(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (!isEligibleMiningDroidUser(self, player) || params == null || !utils.hasScriptVar(self, VAR_LAUNCH_COUNTDOWN))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int step = params.getInt("step");
        if (step > 0)
        {
            utils.setScriptVar(self, VAR_LAUNCH_COUNTDOWN, step);
            sendSystemMessage(player, Integer.toString(step), null);
            dictionary countdown = new dictionary();
            countdown.put("step", step - 1);
            messageTo(self, "handlePlanetaryMiningDroidLaunchCountdown", countdown, 1.0f, false);
            return SCRIPT_CONTINUE;
        }

        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceAvailable(self, resourceType))
        {
            sendSystemMessage(player, "Your droid can no longer find the resource.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = getMiningAmount(self, player);
        if (amount < 1)
        {
            sendSystemMessage(player, "This droid's base extraction rate is too low to return a resource unit at the 90% cap.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_LAUNCH_AMOUNT, amount);
        if (hasObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE) || buff.hasBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE))
        {
            sendSystemMessage(player, "Your Survey License is occupied by an active Interplanetary Mining Droid.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int jobSequence = getIntObjVar(player, VAR_JOB_SEQUENCE) + 1;
        setObjVar(player, VAR_JOB_SEQUENCE, jobSequence);
        String operationId = "reserve:" + jobSequence;
        setObjVar(player, VAR_PENDING_LAUNCH_ITEM, self);
        setObjVar(player, VAR_PENDING_LAUNCH_OPERATION, operationId);
        utils.setScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE, jobSequence);
        if (!planetaryMiningDroidAdjustAccountFeatureId(player, player, 1, operationId))
        {
            clearPendingLaunch(player, operationId);
            sendSystemMessage(player, "The Interplanetary Mining Droid could not reserve an account mining slot.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING, 1);
        sendSystemMessage(player, "Reserving an interplanetary mining slot...", null);
        return SCRIPT_CONTINUE;
    }

    public int handlePlanetaryMiningDroidAccountFeatureResponse(obj_id self, dictionary params) throws InterruptedException
    {
        if (!utils.hasScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING))
        {
            return SCRIPT_CONTINUE;
        }
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        int jobSequence = utils.getIntScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        String operationId = "reserve:" + jobSequence;
        if (params == null || !operationId.equals(params.getString("operationId")))
        {
            return SCRIPT_CONTINUE;
        }
        if (params == null || !params.getBoolean("success") || !isIdValid(player))
        {
            if (isIdValid(player))
            {
                clearPendingLaunch(player, operationId);
                sendSystemMessage(player, "This account already has three active Interplanetary Mining Droid jobs.", null);
            }
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (params.getInt("newValue") > 3)
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, "This account already has three active Interplanetary Mining Droid jobs.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (!isEligibleMiningDroidUser(self, player) || !isAllowedSurveyGrid(planet, surveyLocation) || !isSelectedResourceAvailable(self, resourceType))
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, "Your droid can no longer find the resource.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        int amount = utils.getIntScriptVar(self, VAR_LAUNCH_AMOUNT);
        if (amount < 1)
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (!buff.applyBuff(player, resource.BUFF_PLANETARY_MINING_SURVEY_LICENSE, getMiningTime(self)))
        {
            clearPendingLaunch(player, operationId);
            queuePlanetaryMiningDroidRelease(player, jobSequence);
            sendSystemMessage(player, "The Interplanetary Mining Droid could not occupy your Survey License.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        setObjVar(player, resource.VAR_PLANETARY_MINING_SURVEY_LICENSE, 1);
        dictionary data = new dictionary();
        data.put("resourceType", resourceType);
        data.put("amount", amount);
        data.put("jobSequence", jobSequence);
        setObjVar(player, VAR_ACTIVE_JOB_SEQUENCE, jobSequence);
        clearPendingLaunch(player, operationId);
        messageTo(player, "handlePlanetaryMiningDroidReturn", data, getMiningTime(self), true);
        cleanScriptVars(self);
        consumeCharge(self);
        sendSystemMessage(player, "Your droid has departed on its expedition.", null);
        return SCRIPT_CONTINUE;
    }

    public void clearPendingLaunch(obj_id player, String operationId) throws InterruptedException
    {
        if (operationId.equals(getStringObjVar(player, VAR_PENDING_LAUNCH_OPERATION)))
        {
            removeObjVar(player, VAR_PENDING_LAUNCH_ITEM);
            removeObjVar(player, VAR_PENDING_LAUNCH_OPERATION);
        }
    }

    public void queuePlanetaryMiningDroidRelease(obj_id player, int jobSequence) throws InterruptedException
    {
        dictionary data = new dictionary();
        data.put("jobSequence", jobSequence);
        messageTo(player, "queuePlanetaryMiningDroidRelease", data, 0.0f, true);
    }

    public void showMiningResourceConfirmation(obj_id self, obj_id player, resource_density activeResource, location selectedSite) throws InterruptedException
    {
        obj_id resourceType = activeResource.getResourceType();
        float displayX = selectedSite.x;
        float displayZ = selectedSite.z;
        if (selectedSite.area.equals("mustafar"))
        {
            displayX += 2880.0f;
            displayZ -= 2976.0f;
        }
        else if (selectedSite.area.equals("kashyyyk_rryatt_trail"))
        {
            displayX -= RRYATT_TRAIL_WORLD_OFFSET_X;
            displayZ -= RRYATT_TRAIL_WORLD_OFFSET_Z;
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Resource Name: ").append(getLocalizedResourceName(resourceType));
        prompt.append("\nType: ").append(getLocalizedResourceTypeName(resourceType));
        prompt.append("\nHighest Concentration: ").append(Math.round(activeResource.getDensity() * 100)).append("%");
        prompt.append("\nLocation: (").append(displayX).append(", ").append(displayZ).append(")");
        prompt.append("\n\nResource Stats:");
        resource_attribute[] resourceAttributes = getResourceAttributes(resourceType);
        if (resourceAttributes != null)
        {
            for (resource_attribute resourceAttribute : resourceAttributes)
            {
                prompt.append("\n").append(localize(new string_id("obj_attr_n", resourceAttribute.getName()))).append(": ").append(resourceAttribute.getValue());
            }
        }
        prompt.append("\n\nLaunch the Interplanetary Mining Droid?");
        int pid = sui.createSUIPage(sui.SUI_MSGBOX, self, player, "handleMiningResourceConfirm");
        if (pid >= 0)
        {
            sui.setSUIProperty(pid, sui.MSGBOX_TITLE, sui.PROP_TEXT, DISPLAY_NAME);
            sui.setSUIProperty(pid, sui.MSGBOX_PROMPT, sui.PROP_TEXT, prompt.toString());
            sui.msgboxButtonSetup(pid, sui.OK_CANCEL);
            sui.setSUIProperty(pid, sui.MSGBOX_BTN_CANCEL, sui.PROP_TEXT, "@back");
            sui.showSUIPage(pid);
            trackSui(self, player, pid);
        }
        else
        {
            cleanScriptVars(self);
        }
    }

    public boolean isListSelectionValid(dictionary params) throws InterruptedException
    {
        return params != null && !params.isEmpty() && sui.getIntButtonPressed(params) != sui.BP_CANCEL && sui.getListboxSelectedRow(params) >= 0;
    }

    public void startPmdSurveys(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        obj_id[] candidateTypes = getAvailablePmdResourceTypes(surveyLocation, resourceClass);
        if (candidateTypes == null || candidateTypes.length == 0)
        {
            sendSystemMessage(player, "No resources of that type are currently available at those coordinates.", null);
            cleanScriptVars(self);
            return;
        }
        utils.setScriptVar(self, VAR_SURVEY_CANDIDATES, candidateTypes);
        utils.setScriptVar(self, VAR_SURVEY_INDEX, 0);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_TYPES, new obj_id[0]);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_DENSITIES, new float[0]);
        utils.setScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS, new location[0]);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
    }

    public void requestNextPmdSurvey(obj_id self, obj_id player, String resourceClass, String planet, location surveyLocation) throws InterruptedException
    {
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (!isEligibleMiningDroidUser(self, player) || candidates == null || surveyIndex < 0)
        {
            cleanScriptVars(self);
            return;
        }
        if (surveyIndex >= candidates.length)
        {
            showPmdSurveyResults(self, player);
            return;
        }
        obj_id resourceType = candidates[surveyIndex];
        utils.setScriptVar(self, VAR_SURVEY_INDEX, surveyIndex + 1);
        String resourceName = getResourceName(resourceType);
        if (!requestPmdSurvey(player, self, resourceClass, resourceName, planet, surveyLocation.x, surveyLocation.z))
        {
            cleanScriptVars(self);
        }
    }

    public int OnSurveyDataReceived(obj_id self, float[] xVals, float[] zVals, float[] efficiencies) throws InterruptedException
    {
        obj_id player = utils.getContainingPlayer(self);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        String planet = utils.getStringScriptVar(self, VAR_PLANET);
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id[] candidates = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_CANDIDATES);
        int surveyIndex = utils.getIntScriptVar(self, VAR_SURVEY_INDEX);
        if (!isEligibleMiningDroidUser(self, player) || candidates == null || surveyIndex <= 0 || surveyIndex > candidates.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (xVals != null && zVals != null && efficiencies != null && xVals.length == 9 && zVals.length == 9 && efficiencies.length == 9)
        {
            float bestDensity = 0.0f;
            int bestIndex = -1;
            for (int i = 0; i < efficiencies.length; ++i)
            {
                float efficiency = efficiencies[i];
                if (efficiency > bestDensity)
                {
                    bestDensity = efficiency;
                    bestIndex = i;
                }
            }
            if (bestIndex >= 0 && bestDensity >= MIN_ACTIVE_DENSITY)
            {
                location bestLocation = new location(xVals[bestIndex], 0, zVals[bestIndex], planet);
                if (isAllowedSurveySample(planet, bestLocation))
                {
                    obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
                    float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
                    location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
                    int resultCount = resultTypes.length;
                    obj_id[] updatedTypes = new obj_id[resultCount + 1];
                    float[] updatedDensities = new float[resultCount + 1];
                    location[] updatedLocations = new location[resultCount + 1];
                    System.arraycopy(resultTypes, 0, updatedTypes, 0, resultCount);
                    System.arraycopy(resultDensities, 0, updatedDensities, 0, resultCount);
                    System.arraycopy(resultLocations, 0, updatedLocations, 0, resultCount);
                    updatedTypes[resultCount] = candidates[surveyIndex - 1];
                    updatedDensities[resultCount] = bestDensity;
                    updatedLocations[resultCount] = bestLocation;
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_TYPES, updatedTypes);
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_DENSITIES, updatedDensities);
                    utils.setScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS, updatedLocations);
                }
            }
        }
        requestNextPmdSurvey(self, player, resourceClass, planet, surveyLocation);
        return SCRIPT_CONTINUE;
    }

    public void showPmdSurveyResults(obj_id self, obj_id player) throws InterruptedException
    {
        obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        if (resultTypes == null || resultDensities == null || resultLocations == null || resultTypes.length == 0 || resultTypes.length != resultDensities.length || resultTypes.length != resultLocations.length)
        {
            sendSystemMessage(player, "No resources of that type are currently available at those coordinates.", null);
            cleanScriptVars(self);
            return;
        }
        String[] results = new String[resultTypes.length];
        for (int i = 0; i < resultTypes.length; ++i)
        {
            results[i] = getLocalizedResourceName(resultTypes[i]) + ": " + getLocalizedResourceTypeName(resultTypes[i]) + " - " + Math.round(resultDensities[i] * 100) + "%";
        }
        trackSui(self, player, sui.listbox(self, player, "Survey results. Select a resource to launch the droid.", sui.OK_CANCEL, DISPLAY_NAME, results, "handleMiningResourceSelection"));
    }

    public int handleMiningResourceSelection(obj_id self, dictionary params) throws InterruptedException
    {
        obj_id player = params == null ? obj_id.NULL_ID : sui.getPlayerId(params);
        if (!isCurrentSui(self, player, params))
        {
            return SCRIPT_CONTINUE;
        }
        clearTrackedSui(self, player);
        if (params == null || params.isEmpty())
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getIntButtonPressed(params) == sui.BP_CANCEL)
        {
            consumeCharge(self);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        if (sui.getListboxSelectedRow(params) < 0)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id[] resultTypes = utils.getObjIdArrayScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        float[] resultDensities = utils.getFloatArrayScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        location[] resultLocations = utils.getLocationArrayScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        int index = sui.getListboxSelectedRow(params);
        if (!isEligibleMiningDroidUser(self, player) || resultTypes == null || resultDensities == null || resultLocations == null || index < 0 || index >= resultTypes.length || resultTypes.length != resultDensities.length || resultTypes.length != resultLocations.length)
        {
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        obj_id resourceType = resultTypes[index];
        location bestLocation = resultLocations[index];
        if (!isAllowedSurveyGrid(utils.getStringScriptVar(self, VAR_PLANET), utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION)) || !isIdValid(resourceType) || !isAllowedSurveySample(utils.getStringScriptVar(self, VAR_PLANET), bestLocation))
        {
            sendSystemMessage(player, "That resource is no longer active on the selected planet.", null);
            cleanScriptVars(self);
            return SCRIPT_CONTINUE;
        }
        utils.setScriptVar(self, VAR_RESOURCE_TYPE, resourceType);
        utils.setScriptVar(self, VAR_SURVEY_BEST_DENSITY, resultDensities[index]);
        utils.setScriptVar(self, VAR_SURVEY_BEST_LOCATION, bestLocation);
        utils.setScriptVar(self, VAR_SURVEY_SELECTED, 1);
        showMiningResourceConfirmation(self, player, new resource_density(resourceType, resultDensities[index]), bestLocation);
        return SCRIPT_CONTINUE;
    }

    public String getLocalizedResourceName(obj_id resourceType) throws InterruptedException
    {
        return utils.localizeSIDString(getResourceName(resourceType));
    }

    public String getLocalizedResourceTypeName(obj_id resourceType) throws InterruptedException
    {
        String resourceClass = getResourceClass(resourceType);
        return utils.localizeSIDString(getResourceClassName(resourceClass));
    }

    public boolean isEligibleMiningDroidUser(obj_id self, obj_id player) throws InterruptedException
    {
        return isIdValid(player) && utils.isNestedWithin(self, player) && utils.isProfession(player, utils.TRADER) && getTopMostContainer(player) == player;
    }

    public boolean isAllowedSurveyLocation(String planet, location surveyLocation) throws InterruptedException
    {
        if (surveyLocation == null || planet == null || !planet.equals(surveyLocation.area))
        {
            return false;
        }
        if (planet.equals("mustafar"))
        {
            float displayX = surveyLocation.x + 2880.0f;
            float displayZ = surveyLocation.z - 2976.0f;
            return displayX >= MUSTAFAR_DISPLAY_MIN_COORDINATE + SURVEY_GRID_RADIUS && displayX <= MUSTAFAR_DISPLAY_MAX_COORDINATE - SURVEY_GRID_RADIUS && displayZ >= MUSTAFAR_DISPLAY_MIN_COORDINATE + SURVEY_GRID_RADIUS && displayZ <= MUSTAFAR_DISPLAY_MAX_COORDINATE - SURVEY_GRID_RADIUS;
        }
        float maximumCoordinate = 0.0f;
        if (planet.equals("kashyyyk_main"))
        {
            maximumCoordinate = 4096.0f;
        }
        else if (planet.equals("kashyyyk_dead_forest") || planet.equals("kashyyyk_hunting"))
        {
            maximumCoordinate = 2048.0f;
        }
        else if (planet.equals("kashyyyk_rryatt_trail"))
        {
            maximumCoordinate = 8000.0f;
        }
        if (maximumCoordinate > 0.0f)
        {
            if (surveyLocation.x < -maximumCoordinate + SURVEY_GRID_RADIUS || surveyLocation.x > maximumCoordinate - SURVEY_GRID_RADIUS || surveyLocation.z < -maximumCoordinate + SURVEY_GRID_RADIUS || surveyLocation.z > maximumCoordinate - SURVEY_GRID_RADIUS)
            {
                return false;
            }
            if (planet.equals("kashyyyk_main"))
            {
                return isInAnyPolygon(surveyLocation.x, surveyLocation.z, KASHYYYK_MAIN_SURVEY_REGIONS);
            }
            if (planet.equals("kashyyyk_rryatt_trail"))
            {
                return isInAnyPolygon(surveyLocation.x, surveyLocation.z, KASHYYYK_RRYATT_TRAIL_SURVEY_REGIONS);
            }
            if (planet.equals("kashyyyk_dead_forest"))
            {
                return isInAnyPolygon(surveyLocation.x, surveyLocation.z, KASHYYYK_DEAD_FOREST_SURVEY_REGIONS);
            }
            if (planet.equals("kashyyyk_hunting"))
            {
                return isInAnyPolygon(surveyLocation.x, surveyLocation.z, KASHYYYK_HUNTING_SURVEY_REGIONS);
            }
            return true;
        }
        for (String kashyyykScene : KASHYYYK_SCENES)
        {
            if (planet.equals(kashyyykScene))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAllowedSurveyGrid(String planet, location surveyLocation) throws InterruptedException
    {
        if (!isWithinSceneBounds(planet, surveyLocation, SURVEY_GRID_RADIUS))
        {
            return false;
        }
        if (planet.equals("kashyyyk_main"))
        {
            return isAllowedKashyyykMainSurveyCenter(surveyLocation);
        }
        if (planet.equals("kashyyyk_rryatt_trail"))
        {
            return isWithinAnyPolygonOutset(surveyLocation.x, surveyLocation.z, KASHYYYK_RRYATT_TRAIL_SURVEY_REGIONS, KASHYYYK_MAIN_SURVEY_EDGE_OUTSET);
        }
        if (planet.equals("kashyyyk_dead_forest"))
        {
            return isWithinAnyPolygonOutset(surveyLocation.x, surveyLocation.z, KASHYYYK_DEAD_FOREST_SURVEY_REGIONS, KASHYYYK_MAIN_SURVEY_EDGE_OUTSET);
        }
        if (planet.equals("kashyyyk_hunting"))
        {
            return isWithinAnyPolygonOutset(surveyLocation.x, surveyLocation.z, KASHYYYK_HUNTING_SURVEY_REGIONS, KASHYYYK_MAIN_SURVEY_EDGE_OUTSET);
        }
        for (float x = surveyLocation.x - SURVEY_GRID_RADIUS; x <= surveyLocation.x + SURVEY_GRID_RADIUS; x += SURVEY_GRID_RADIUS)
        {
            for (float z = surveyLocation.z - SURVEY_GRID_RADIUS; z <= surveyLocation.z + SURVEY_GRID_RADIUS; z += SURVEY_GRID_RADIUS)
            {
                if (!isAllowedSurveySample(planet, new location(x, 0, z, surveyLocation.area)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isAllowedKashyyykMainSurveyCenter(location surveyLocation) throws InterruptedException
    {
        return isWithinAnyPolygonOutset(surveyLocation.x, surveyLocation.z, KASHYYYK_MAIN_SURVEY_REGIONS, KASHYYYK_MAIN_SURVEY_EDGE_OUTSET) && !isInAnyPolygon(surveyLocation.x, surveyLocation.z, KASHYYYK_MAIN_SURVEY_EXCLUSION_REGIONS);
    }

    public boolean isAllowedSurveySample(String planet, location surveyLocation) throws InterruptedException
    {
        return isWithinSceneBounds(planet, surveyLocation, 0.0f);
    }

    public boolean isWithinSceneBounds(String planet, location surveyLocation, float inset) throws InterruptedException
    {
        if (surveyLocation == null || planet == null || !planet.equals(surveyLocation.area))
        {
            return false;
        }
        if (planet.equals("mustafar"))
        {
            float displayX = surveyLocation.x + 2880.0f;
            float displayZ = surveyLocation.z - 2976.0f;
            return displayX >= MUSTAFAR_DISPLAY_MIN_COORDINATE + inset && displayX <= MUSTAFAR_DISPLAY_MAX_COORDINATE - inset && displayZ >= MUSTAFAR_DISPLAY_MIN_COORDINATE + inset && displayZ <= MUSTAFAR_DISPLAY_MAX_COORDINATE - inset;
        }
        float maximumCoordinate = planet.equals("kashyyyk_rryatt_trail") ? 8000.0f : ((planet.equals("kashyyyk_dead_forest") || planet.equals("kashyyyk_hunting")) ? 2048.0f : (planet.equals("kashyyyk_main") ? 4096.0f : 0.0f));
        return maximumCoordinate > 0.0f && surveyLocation.x >= -maximumCoordinate + inset && surveyLocation.x <= maximumCoordinate - inset && surveyLocation.z >= -maximumCoordinate + inset && surveyLocation.z <= maximumCoordinate - inset;
    }

    public boolean isWithinPolygonOutset(float x, float z, float[][] polygon, float outset)
    {
        float outsetSquared = outset * outset;
        for (int current = 0, previous = polygon.length - 1; current < polygon.length; previous = current++)
        {
            float startX = polygon[previous][0];
            float startZ = polygon[previous][1];
            float deltaX = polygon[current][0] - startX;
            float deltaZ = polygon[current][1] - startZ;
            float edgeLengthSquared = deltaX * deltaX + deltaZ * deltaZ;
            float projection = ((x - startX) * deltaX + (z - startZ) * deltaZ) / edgeLengthSquared;
            projection = Math.max(0.0f, Math.min(1.0f, projection));
            float nearestX = startX + projection * deltaX;
            float nearestZ = startZ + projection * deltaZ;
            float distanceX = x - nearestX;
            float distanceZ = z - nearestZ;
            if (distanceX * distanceX + distanceZ * distanceZ <= outsetSquared)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInAnyPolygon(float x, float z, float[][][] polygons)
    {
        for (float[][] polygon : polygons)
        {
            if (isInPolygon(x, z, polygon))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isWithinAnyPolygonOutset(float x, float z, float[][][] polygons, float outset)
    {
        for (float[][] polygon : polygons)
        {
            if (isInPolygon(x, z, polygon) || isWithinPolygonOutset(x, z, polygon, outset))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInPolygon(float x, float z, float[][] polygon)
    {
        boolean inside = false;
        for (int current = 0, previous = polygon.length - 1; current < polygon.length; previous = current++)
        {
            float currentX = polygon[current][0];
            float currentZ = polygon[current][1];
            float previousX = polygon[previous][0];
            float previousZ = polygon[previous][1];
            float crossProduct = (x - currentX) * (previousZ - currentZ) - (z - currentZ) * (previousX - currentX);
            if (Math.abs(crossProduct) < 0.001f && x >= Math.min(currentX, previousX) && x <= Math.max(currentX, previousX) && z >= Math.min(currentZ, previousZ) && z <= Math.max(currentZ, previousZ))
            {
                return true;
            }
            if ((currentZ > z) != (previousZ > z) && x < (previousX - currentX) * (z - currentZ) / (previousZ - currentZ) + currentX)
            {
                inside = !inside;
            }
        }
        return inside;
    }

    public resource_density getResourceDensity(obj_id self, obj_id resourceType) throws InterruptedException
    {
        obj_id selectedResource = utils.getObjIdScriptVar(self, VAR_RESOURCE_TYPE);
        if (isIdValid(resourceType) && resourceType.equals(selectedResource) && utils.hasScriptVar(self, VAR_SURVEY_BEST_DENSITY))
        {
            return new resource_density(resourceType, utils.getFloatScriptVar(self, VAR_SURVEY_BEST_DENSITY));
        }
        return null;
    }

    public boolean isSelectedResourceAvailable(obj_id self, obj_id resourceType) throws InterruptedException
    {
        if (getResourceDensity(self, resourceType) == null)
        {
            return false;
        }
        location surveyLocation = utils.getLocationScriptVar(self, VAR_SURVEY_LOCATION);
        String resourceClass = utils.getStringScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        if (surveyLocation == null || resourceClass == null || resourceClass.equals(""))
        {
            return false;
        }
        obj_id[] availableResources = getAvailablePmdResourceTypes(surveyLocation, resourceClass);
        if (availableResources != null)
        {
            for (obj_id availableResource : availableResources)
            {
                if (resourceType.equals(availableResource))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public int getMiningTime(obj_id self) throws InterruptedException
    {
        return 30;
    }

    public int getMiningAmount(obj_id self, obj_id player) throws InterruptedException
    {
        float baseExtractionRate = getFloatObjVar(self, ATTRIBUTE_EXTRACTION_RATE);
        if (baseExtractionRate <= 0)
        {
            baseExtractionRate = getFloatObjVar(self, LEGACY_STATIC_ATTRIBUTE_BASE + "extractRate");
        }
        int amount = (int)(baseExtractionRate * 0.9f);
        int expertiseResourceIncrease = getSkillStatisticModifier(player, "expertise_resource_sampling_increase");
        if (expertiseResourceIncrease > 0)
        {
            amount += (int)(amount * expertiseResourceIncrease / 100.0f);
        }
        if (buff.hasBuff(player, "tcg_series4_falleens_fist"))
        {
            amount = (int)(amount * 1.5f);
        }
        return amount;
    }

    public void consumeCharge(obj_id self) throws InterruptedException
    {
        int charges = getCount(self);
        if (charges > 1)
        {
            incrementCount(self, -1);
        }
        else
        {
            destroyObject(self);
        }
    }

    public boolean beginFlow(obj_id self, obj_id player) throws InterruptedException
    {
        if (utils.hasScriptVar(self, VAR_FLOW_PLAYER) || hasObjVar(player, VAR_PENDING_LAUNCH_ITEM) || hasObjVar(player, VAR_PENDING_RELEASE_SEQUENCES) || utils.hasScriptVar(player, VAR_RELEASE_IN_FLIGHT))
        {
            return false;
        }
        obj_id activeItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
        if (isIdValid(activeItem))
        {
            return false;
        }
        utils.removeScriptVar(player, VAR_FLOW_ITEM);
        utils.setScriptVar(self, VAR_FLOW_PLAYER, player);
        utils.setScriptVar(player, VAR_FLOW_ITEM, self);
        return true;
    }

    public void trackSui(obj_id self, obj_id player, int pid) throws InterruptedException
    {
        if (pid < 0)
        {
            cleanScriptVars(self);
            return;
        }
        utils.setScriptVar(self, VAR_FLOW_PID, pid);
        sui.setPid(player, pid, PID_NAME);
    }

    public boolean isCurrentSui(obj_id self, obj_id player, dictionary params) throws InterruptedException
    {
        if (params == null || params.isEmpty() || !isIdValid(player))
        {
            return false;
        }
        obj_id flowPlayer = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        obj_id flowItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
        if (!player.equals(flowPlayer) || !self.equals(flowItem) || !utils.hasScriptVar(self, VAR_FLOW_PID) || !sui.hasPid(player, PID_NAME))
        {
            return false;
        }
        int pageId = params.getInt("pageId");
        int itemPid = utils.getIntScriptVar(self, VAR_FLOW_PID);
        int playerPid = sui.getPid(player, PID_NAME);
        if (pageId != itemPid || pageId != playerPid)
        {
            if (pageId >= 0)
            {
                forceCloseSUIPage(pageId);
            }
            return false;
        }
        if (!utils.isNestedWithin(self, player))
        {
            cleanScriptVars(self);
            return false;
        }
        return true;
    }

    public void clearTrackedSui(obj_id self, obj_id player) throws InterruptedException
    {
        int itemPid = utils.getIntScriptVar(self, VAR_FLOW_PID);
        if (isIdValid(player) && sui.hasPid(player, PID_NAME) && sui.getPid(player, PID_NAME) == itemPid)
        {
            sui.removePid(player, PID_NAME);
        }
        utils.removeScriptVar(self, VAR_FLOW_PID);
    }

    public void cleanScriptVars(obj_id self) throws InterruptedException
    {
        obj_id player = utils.getObjIdScriptVar(self, VAR_FLOW_PLAYER);
        if (utils.hasScriptVar(self, VAR_FLOW_PID))
        {
            int pid = utils.getIntScriptVar(self, VAR_FLOW_PID);
            if (pid >= 0)
            {
                forceCloseSUIPage(pid);
            }
        }
        if (isIdValid(player))
        {
            if (sui.hasPid(player, PID_NAME))
            {
                sui.removePid(player, PID_NAME);
            }
            obj_id flowItem = utils.getObjIdScriptVar(player, VAR_FLOW_ITEM);
            if (self.equals(flowItem))
            {
                utils.removeScriptVar(player, VAR_FLOW_ITEM);
            }
        }
        utils.removeScriptVar(self, VAR_FLOW_PLAYER);
        utils.removeScriptVar(self, VAR_FLOW_PID);
        utils.removeScriptVar(self, VAR_LAUNCH_COUNTDOWN);
        utils.removeScriptVar(self, VAR_ACCOUNT_RESERVATION_PENDING);
        utils.removeScriptVar(self, VAR_LAUNCH_AMOUNT);
        utils.removeScriptVar(self, VAR_LAUNCH_JOB_SEQUENCE);
        utils.removeScriptVar(self, VAR_PLANET);
        utils.removeScriptVar(self, VAR_SURVEY_LOCATION);
        utils.removeScriptVar(self, VAR_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_SELECTED_RESOURCE_CLASS);
        utils.removeScriptVar(self, VAR_SURVEY_CANDIDATES);
        utils.removeScriptVar(self, VAR_SURVEY_INDEX);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_TYPES);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_DENSITIES);
        utils.removeScriptVar(self, VAR_SURVEY_RESULT_LOCATIONS);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_DENSITY);
        utils.removeScriptVar(self, VAR_SURVEY_BEST_LOCATION);
        utils.removeScriptVar(self, VAR_RESOURCE_TYPE);
        utils.removeScriptVar(self, VAR_SURVEY_SELECTED);
    }

    public int OnGetAttributes(obj_id self, obj_id player, String[] names, String[] attribs) throws InterruptedException
    {
        int index = utils.getValidAttributeIndex(names);
        if (index == -1)
        {
            return SCRIPT_CONTINUE;
        }
        int charges = getCount(self);
        if (charges > 0)
        {
            names[index] = "quantity";
            attribs[index++] = Integer.toString(charges);
        }
        if (index < names.length && hasObjVar(self, ATTRIBUTE_EXTRACTION_RATE))
        {
            names[index] = "extractRate";
            attribs[index++] = Integer.toString((int)getFloatObjVar(self, ATTRIBUTE_EXTRACTION_RATE));
        }
        if (index < names.length && hasObjVar(self, ATTRIBUTE_BASE + "mechanism_quality"))
        {
            names[index] = "mechanism_quality";
            attribs[index] = Integer.toString((int)getFloatObjVar(self, ATTRIBUTE_BASE + "mechanism_quality"));
        }
        return SCRIPT_CONTINUE;
    }
}
