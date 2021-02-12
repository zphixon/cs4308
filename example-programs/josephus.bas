fun jos(n, k)
    let m = 0
    for i = m + 1, n do
        m = (m + k) % i
    end
    return m
end

print "survivor is number " + jos(41, 3)
