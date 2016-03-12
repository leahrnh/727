import sys

filename = sys.argv[1]
fr = open(filename, 'r')
fw = open('tmp.txt', 'w')

for line in fr:
    if (len(line) > 1):
        line = line[:-1] + "\t_\t_\n"
    fw.write(line)
    
fr.close()
fw.close()


