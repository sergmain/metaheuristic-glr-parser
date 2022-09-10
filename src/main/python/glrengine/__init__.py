# -*- coding: utf-8 -*-
__all__ = ['GLRSplitter' 'GLRAutomaton', 'GLRScanner', 'morph_parser', 'make_scanner']

#  License: Apache 2.0
#  Metaheuristic project - https://github.com/sergmain?tab=projects&type=classic
#  Copyright (c) 2022. Sergio Lissner
#
#
#

from glrengine.splitter import GLRSplitter
from glrengine.automaton import GLRAutomaton
from glrengine.normalizer import morph_parser
from glrengine.scanner import GLRScanner, make_scanner
