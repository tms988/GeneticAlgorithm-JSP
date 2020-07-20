考虑合并machineMatrix、timeMatrix，变成单个Mission类的matrix。

因为涉及到交叉，可以先不管每个任务步骤的编号，等运算结束再套上相应的编号。

##### · Cautions in calculateFitness()

和我的项目不同点：

1）参考里一个任务步骤对应一台设备；项目里一个任务步骤对应一个设备组。
设备组里可以同时运行多个设备。
所以要增加变量：该设备组目前在使用的设备数量`machineUsedNow`、可以使用的数量上限`machineAvailableMax`（即设备组里总共有多少台设备）。
达到上限后，按照参考里的方法处理，即：
`if (machineUsedNow<machineAvailableMax) // 可以直接使用设备组里其余设备
result.startTime[jobId][processId] = (processId==0) ? 0 : result.endTime[jobId][processId - 1];
else // 等待最即将完成运行的设备运行结束
result.startTime[jobId][processId] = (processId==0) ? result.machineWorkTime[machineId] :
                    Math.max(result.endTime[jobId][processId - 1], result.machineWorkTime[machineGroupId].findNearMachine());`
                    
machineGroup.findNearMachine(): 找出最即将完成运行的设备（返回值是？）