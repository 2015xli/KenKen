# A fast solver for KenKen (KenDoku) 

This project is designed for efficiency in both speed and memory.
 
 For SuDoKu solutions, input an integer that represents the matrix size, such as:
 ```java
 java Kenken 5
 ```
 It outputs all the SuDoKu solutions for 5x5 matrix.
 
 For KenKen solutions, provide a file that encodes a KenKen problem, such as:
 ```java
 java Kenken input/kenken.5
 ```
 It uses file ./input/kenken.5 as input and computes its solution.
 
The package includes a few example files for KenKen problems under ./input directory. 
Please just follow them to create your own KenKen problems.
For example, for the following KenKen problem:

![A 6x6 KenKen problem](./input/input.6.png)

The input file input.6 has the following, which uses a _single_ character to represent one region, and uses operator + - * / to represent the computation. If it is a single cell region that has no computation, leave the operator blank. Multiplication operator can be either * or x .
```
K K B B B B
a c l s s t
a c l $ # t
a d m $ # z
b d m v x z
b w w v x z

K 6 x
B 14 +
a 10 +
c 1 -
l 7 +
s 4 x
t 2 /
d 1 -
m 1 -
$ 30 x
# 5 +
b 3 -
w 3 -
v 7 +
x 6 x
z 30 x
```
