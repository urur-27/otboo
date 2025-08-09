package com.team3.otboo.converter;

public class GridConverter {
    // 지구 반경 등 상수 (5km 격자 기준)
    private static final double RE = 6371.00877;       // 지구 반경 (km)
    private static final double GRID = 5.0;            // 격자 간격 (km)
    private static final double SLAT1 = 30.0;          // 투영 위도1
    private static final double SLAT2 = 60.0;          // 투영 위도2
    private static final double OLON = 126.0;          // 기준 경도
    private static final double OLAT = 38.0;           // 기준 위도
    private static final double XO = 210 / GRID;       // 기준점 X좌표 (origin)
    private static final double YO = 675 / GRID;       // 기준점 Y좌표 (origin)

    public static int[] latLonToGrid(double lat, double lon) {
        double degToRad = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * degToRad;
        double slat2 = SLAT2 * degToRad;
        double olon  = OLON  * degToRad;
        double olat  = OLAT  * degToRad;

        // 투영 상수 계산
        double sn = Math.tan(Math.PI*0.25 + slat2*0.5) / Math.tan(Math.PI*0.25 + slat1*0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI*0.25 + slat1*0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI*0.25 + olat*0.5);
        ro = re * sf / Math.pow(ro, sn);

        // 입력 위경도 → 투영
        double ra = Math.tan(Math.PI*0.25 + (lat * degToRad)*0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * degToRad - olon;
        if (theta > Math.PI)  theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        // 실수형 격자 좌표
        double x = ra * Math.sin(theta) + XO;
        double y = ro - ra * Math.cos(theta) + YO;

        // 반올림하여 정수로
        int ix = (int)(x + 1.5);
        int iy = (int)(y + 1.5);

        return new int[]{ ix, iy };
    }
}

