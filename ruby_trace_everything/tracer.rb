# from Russ Olsen's book: Eloquent Ruby 
proc_object = proc do |event, file, line, id, binding, klass| 
  puts "#{event} in #{file}/#{line} #{id} #{klass}"
end 

set_trace_func(proc_object)
