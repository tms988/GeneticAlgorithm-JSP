public class testttt {
    public static void main(String[] args) {
     int[] arr = {5,7,1,3,2,6,4,8};
     quicksort(arr, 0, arr.length);
    }

    public static void quicksort(int[] arr, int l, int r){
        if (l<r) {
            int pos = partition(arr, l, r);
        }

    }

    public static int partition(int[] arr, int l, int r) {
        int key = arr[l];
        while (l<r) {
            while (l<r && arr[r]>=key) r--;
            if (l<r) {
                arr[l] = arr[r];
            }
            while (l<r && arr[l]<=key) l++;
            if (l<r) {
                arr[r] = arr[l];
            }
        }
        arr[l] = key;
        return l;
    }
}
