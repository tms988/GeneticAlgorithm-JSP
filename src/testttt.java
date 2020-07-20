public class testttt {
    public static void main(String[] args) {
        String s = "pwwkew";
        int len = s.length();
        int[] dp = new int[len];
        dp[0] = 1;
        int res = 1;
        for (int i=1; i<len; i++) {
            if (s.charAt(i)==s.charAt(i-1)) {
                dp[i] = 1;
                continue;
            }
            int a = s.indexOf(s.charAt(i), i-dp[i-1]);
            if (a==-1 || a==i) {
                dp[i] = dp[i-1]+1;
            }
            else { // 有重复
                dp[i] = i-a;
            }
            System.out.println("dp="+dp[i]);
            res = Math.max(res, dp[i]);
        }
        System.out.println(res);
    }
}
