fun gcd(a, b)
    let factor = b
    if a > b then
        factor = a
    end

    let gcd = 1
    for i = factor, 1, -1 do
        if a % i == 0 and b % i == 0 then
            gcd = i
        end
    end

    return gcd
end

print gcd(49, 7)
