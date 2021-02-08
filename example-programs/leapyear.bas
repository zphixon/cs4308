function leapYear(year)
    if year % 4 != 0 then
        return false
    elseif year % 100 != 0 then
        return true
    elseif year % 400 != 0 then
        return false
    else
        return true
    end
end

print "Enter a year"
input year
if leapYear(year) then
    print year + " is a leap year"
else
    print year + " is not a leap year"
end
