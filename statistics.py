#統計関数
import numpy
class statistics:
    def deviation(num_array_data):#偏差
        ans_array_data = []
        avg_dt = sum(num_array_data) / len (num_array_data)
        for i in num_array_data:
            ans_array_data.append(i - avg_dt)
        return ans_array_data

    def deviation_sum_of_squares(num_array_data):#偏差平方和
        ans = []
        ans_array_data = statistics.deviation(num_array_data)
        for i in ans_array_data:
            ans.append(pow(i, 2))
        ans = sum(ans) / len(num_array_data)
        return ans

    def standard_deviation(num_array_data):#標準偏差
        ans = statistics.deviation_sum_of_squares(num_array_data)
        ans = numpy.sqrt(ans)
        return ans

    def covariance(num_array_data1, num_array_data2):#共分散
        if(len(num_array_data1) == len(num_array_data2)):
            ans = 0
            j = 0
            num_array_data1 = statistics.deviation(num_array_data1)
            num_array_data2 = statistics.deviation(num_array_data2)
            for j, i in enumerate(num_array_data1):
                ans += (i * num_array_data2[j])
            return ans / j
        else:
            print("len(num_array_data1) ≠ len(num_array_data2)")

    def correlation_coefficient(num_array_data1, num_array_data2):#相関係数
        ans = statistics.covariance(num_array_data1, num_array_data2)
        return (ans / (statistics.standard_deviation(num_array_data1) * statistics.standard_deviation(num_array_data2)))



data = [1355, 1614, 1693, 2293, 1832, 2118, 2342]
data2 = [240, 259, 230, 366, 368, 499, 683]

ans = statistics.correlation_coefficient(data, data2)
print("相関係数：", ans)
print(sum(data), sum(data2), sum(statistics.deviation(data)), sum(statistics.deviation(data2)))