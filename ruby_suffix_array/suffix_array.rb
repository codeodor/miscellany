# A suffix Array class for strings.
# Allows rapid searching for substrings.
class SuffixArray
  # Create a suffix array from the supplied string.  If duplicate is
  # duplicate, it will be copied, so that if it is frozen to speed
  # things up this will not affect the string from which it is
  # copied.  If freeze is "freeze" this internal freezing will
  # happen. If your string is very big you may not wish to copy it,
  # and you will probably have already frozen it so as to prevent
  # its modification. If not, then you will need to consider
  # performance.  The default is to duplicate and freeze.
  def initialize(the_string, duplicate = "duplicate", freeze = "freeze")
    # because strings are passed by reference and we are about to
    # freeze this one,..  
    @the_string = the_string.dup unless duplicate !~ /duplicate/i
    # We are not going to modify it, to permit optimisation, we
    # freeze it.
    @the_string.freeze unless freeze !~ /freeze/i
    # We know how big the array must be now.
    @suffix_array = Array.new(@the_string.size)
    
    #build the suffixes 
    @last_index = @the_string.length-1
    (0..@last_index).each do |the_position|
      # << is the append (or push) operator for arrays in Ruby
      ##  No need to hold the suffix in the suffix array, we have
      ##  the index into @the_string.  This would explode memory
      ##  usage enormously, from N characters to N(N-1)/2
      ##  characters.
      ## @suffix_array << { :suffix=>the_suffix, :position=>the_position }
      ##  Instead:
      @suffix_array[the_position] = the_position
      ##  Jon Bentley's Programming Pearls takes almost this approach:
      ##  he uses C pointers instead of integers.
    end
      
    #sort the suffix array, on the lexical ordering of the suffices.
    @suffix_array.sort! { |a,b| suffix_at(a) <=> suffix_at(b)}
  end

  
  def find_first_substring_index(the_substring, n_mismatches = 0) 
    #first one found, not necessarily first on in the string
    finder(the_substring, n_mismatches, true)
  end
  
  def find_all_substring_indices(the_substring, n_mismatches = 0)
    finder(the_substring, n_mismatches, false)
  end
  

  # return [] if there are no matches.
  # return a list of matches if there are matches.
  def find_all_substring_indices_without_mismatch(the_substring)

    # invariant  suffix_at(@suffix_array[lower]) <= the_substring <=
    # suffix_at(@suffix_array[upper])
    lower = 0
    upper = @last_index 
    len = the_substring.size - 1
    
    found = false
    while (lower < upper)
      mid = (lower + upper) / 2
      # puts "#{lower}, #{mid}, #{upper}"
      comparison = suffix_at(@suffix_array[mid])[0..len]
      if (the_substring > comparison)
        lower = mid + 1
      elsif (the_substring < comparison)
        upper = mid
      else
        # We have an exact match.
        found = true
        break
      end
    end

    # puts "#{lower}, #{mid}, #{upper}"
    if found
      # Search left and right for the extents of the range.
      lower = upper = mid
      while (the_substring == suffix_at(@suffix_array[lower-1])[0..len])
        lower -= 1
      end
      while (the_substring == suffix_at(@suffix_array[upper+1])[0..len])
        upper += 1
      end
      list = @suffix_array[lower..upper].sort
    else
      list = []
    end

    return list
  end



  # return [] if there are no matches.
  # return a list of matches if there are matches.
  # Use the double binary search of the wikipedia article.
  # (Tried this before but forgot to trim the substrings, [0..len] so
  # it failed.)  Should be faster than the other one.
  def find_all_substring_indices_double_binary(the_substring)

    # invariant  suffix_at(@suffix_array[lower]) <= the_substring <=
    # suffix_at(@suffix_array[upper])
    lower = 0
    upper = @last_index 
    len = the_substring.size - 1
    
    while (lower < upper)
      mid = (lower + upper) / 2
      # puts "#{lower}, #{mid}, #{upper}"
      if (the_substring > suffix_at(@suffix_array[mid])[0..len])
        lower = mid + 1
      else
        upper = mid
      end
    end
    start = lower
    upper = @last_index
    while (lower < upper)
      mid = (lower + upper) / 2
      # puts "#{lower}, #{mid}, #{upper}"
      if (the_substring ==  suffix_at(@suffix_array[mid])[0..len])
        lower = mid
      else
        upper = mid - 1
      end
    end

    # puts "#{start}, #{lower}, #{mid}, #{upper}"
    list = @suffix_array[start..upper].sort

    return list
  end

  
  private

  # Return the suffix string given the index into the
  # original string  That is, given the value held in
  # the suffix array. Name from the Wikipedia page.
  def suffix_at(n)
    @the_string[n..@last_index]
  end


  def finder(the_substring, n_mismatches, quit_after_first_result)
    results = []
    compare_len = the_substring.length-1
    #uses typical binary search
    high = @suffix_array.length - 1
    low = 0
    while(low <= high)
      mid = (high + low) / 2
      this_suffix = suffix_at(@suffix_array[mid])
      comparison = this_suffix[0..compare_len]
      
      if n_mismatches == 0
        within_n_mismatches = comparison == the_substring
      else
        within_n_mismatches = hamming_distance(the_substring, comparison) <= n_mismatches
      end
      
      if within_n_mismatches
        results << @suffix_array[mid]
        return results[0] if quit_after_first_result
      end
      
      if comparison > the_substring
        high = mid - 1
      else
        low = mid + 1  
      end
      
      # the comparisons order in the original version:
      #if comparison > the_substring
      #  high = mid - 1
      #elsif comparison < the_substring
      #  low = mid + 1
      #else 
      #  return @suffix_array[mid][:position]
      #end
    end
  
    if quit_after_first_result
      return nil 
    else
      return results
    end
  end
  
  def hamming_distance(a, b)
    # from Mladen Jablanović's answer at http://stackoverflow.com/questions/5322428/finding-a-substring-while-allowing-for-mismatches-with-ruby 
    a.chars.zip(b.chars).count{|ca, cb| ca != cb}
  end
  
  
end
  
sa = SuffixArray.new("abracadabra")
puts sa.find_first_substring_index("brd", 1) # outputs 8
puts sa.find_all_substring_indices("brd", 1).inspect # outputs [8, 1]
puts sa.find_first_substring_index("abr", 0) # outputs 0
puts sa.find_all_substring_indices("abr", 0).inspect # outputs [0l, but 
                                                     # not [0,7]...

puts sa.find_all_substring_indices_without_mismatch("abr").inspect 
        # outputs [0,7]

puts sa.find_all_substring_indices_double_binary("abr").inspect 
        # outputs [0,7]

puts sa.find_all_substring_indices("fish", 0).inspect # outputs []

puts sa.find_all_substring_indices_without_mismatch("fish").inspect
        # outputs nil

puts sa.find_all_substring_indices_double_binary("fish").inspect
        # outputs  []

# puts sa.inspect
