set title "Cache Filling in Cyclon"
set xlabel "time"
set ylabel "avg of neighbours in cache"
set xrange [0:30]
set terminal png size 1000,500 enhanced font 'Verdana,12'
set output 'indegree.png'
plot  '-' u 3:5 t "c=20" with lines

  control.cacheo:  time: 0  avg: 1.0
  control.cacheo:  time: 1  avg: 1.0
  control.cacheo:  time: 2  avg: 1.5
  control.cacheo:  time: 3  avg: 3.3655
  control.cacheo:  time: 4  avg: 6.07468
  control.cacheo:  time: 5  avg: 9.7057
  control.cacheo:  time: 6  avg: 13.30122
  control.cacheo:  time: 7  avg: 15.85686
  control.cacheo:  time: 8  avg: 17.48356
  control.cacheo:  time: 9  avg: 18.48744
  control.cacheo:  time: 10  avg: 19.09258
  control.cacheo:  time: 11  avg: 19.45566
  control.cacheo:  time: 12  avg: 19.65412
  control.cacheo:  time: 13  avg: 19.762
  control.cacheo:  time: 14  avg: 19.8185
  control.cacheo:  time: 15  avg: 19.84468
  control.cacheo:  time: 16  avg: 19.85252
  control.cacheo:  time: 17  avg: 19.85304
  control.cacheo:  time: 18  avg: 19.8523
  control.cacheo:  time: 19  avg: 19.84586
  control.cacheo:  time: 20  avg: 19.83428
  control.cacheo:  time: 21  avg: 19.82742
  control.cacheo:  time: 22  avg: 19.82348
  control.cacheo:  time: 23  avg: 19.81702
  control.cacheo:  time: 24  avg: 19.80884
  control.cacheo:  time: 25  avg: 19.80468
  control.cacheo:  time: 26  avg: 19.80108
  control.cacheo:  time: 27  avg: 19.7979
  control.cacheo:  time: 28  avg: 19.79256
  control.cacheo:  time: 29  avg: 19.78832
end
