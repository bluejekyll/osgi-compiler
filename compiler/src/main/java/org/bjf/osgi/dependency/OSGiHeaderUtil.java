/*
 * Copyright (c) 2013, Benjamin J. Fry
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Benjamin J. Fry nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.bjf.osgi.dependency;

import java.util.HashSet;
import java.util.Set;

/**
 * OSGiHeaderUtil
 *
 * @author bfry
 */
public class OSGiHeaderUtil {
    private final String header;

    public OSGiHeaderUtil(String header) {
        this.header = header;
    }
    
    public Set<String> parse(boolean excludeOptional) {
        Set<String> result = new HashSet<String>();

        int end = header.length();
        boolean inQuote = false;
        boolean isOptional = false;
        
        for (int i = header.length() - 1; i >= 0; i--) {
            char curCh = header.charAt(i);

            switch (curCh) {
                case ',':
                    if (!inQuote) {
                        // only add if we aren't excluding optinal, or it isn't optional.
                        if (!excludeOptional || !isOptional) result.add(header.substring(i+1, end));
                        isOptional = false;
                        end = i;
                    }
                    break;
                case ';':
                    if (!inQuote) {
                        String option = header.substring(i+1, end);
                        if ("resolution:=optional".equals(option)) isOptional = true;
                        // might want to add more options stuff here...
                        end = i;
                    }
                    
                    break;
                case '"':
                    inQuote = !inQuote;
                    break;
            }
        }

        // we need the last string if we aren't in either
        if (header.length() > 0) {
            if (!excludeOptional || !isOptional) result.add(header.substring(0, end));
        }

        return result;
    }
}
