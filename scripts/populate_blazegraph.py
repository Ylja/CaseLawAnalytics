'''
Usage:
python populate_blazegraph [-n 1000] [-b 1000] filepath_input filepath_output
filepath_input: root directory of the downloaded dump of rechtspraak.nl
filepath_output: where should the n3 files be stored

n : maximum number of documents to obtain
b : batch size
'''

import sys
import os
sys.path.insert(0, os.path.abspath('..'))
from rechtspraak_parser import populate_blazegraph

if __name__ == "__main__":
    import sys
    import getopt

    optlist, args = getopt.getopt(sys.argv[1:], 'n:b:')
    optdict = dict(optlist)
    filepath_input = args[0]
    filepath_output = args[1]
    nrdocs = int(optdict.get('-n', 30000))
    batchsize = int(optdict.get('-b', 1000))
    print(filepath_input, filepath_output, nrdocs, batchsize)
    populate_blazegraph.parse_data_in_batches(filepath_input, filepath_output,
                                 nrdocs=nrdocs, batchsize=batchsize)