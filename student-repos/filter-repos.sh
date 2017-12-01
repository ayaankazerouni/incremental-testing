# Remove repositories 10 or fewer commits.
# They won't give useful information, and this reduces the dataset 
# by quite a bit.
outerdir=$1

cd $outerdir

for d in */; do
	cd "${d}"
	count=$(git count)
	cd ..
	if (( "${count}" < 11)); then
		echo "Removing ${d} with ${count} commits."
		rm -rf ${d}
	fi
done
