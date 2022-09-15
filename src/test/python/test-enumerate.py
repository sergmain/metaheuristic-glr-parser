#  License: Apache 2.0
#  Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
#  Copyright (c) 2022. Sergio Lissner
#


list = [1,2,3,4]

k = 0
for i in enumerate(list):
    print(i)

    list = [6]
    if k>10:
        break

    k += 1
