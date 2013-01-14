#!/usr/bin/ruby

# The number of transmitters to place
txSizes = [20, 40, 60, 80, 100];
# The random seeds to use to generate values
seeds = [1]

# Dimensions of the "transmitter" (inner) rectangle
txDimension = [10, 10]
# Dimensions of the "receiver" (outer) rectangle
rxDimension = [10, 10]

# Algorithm names to use
algorithms = ['basic', 'binned', 'grid', 'recursive']

# With a density of 35
# grid = 350x350 = 122,500 points
# recursive = 35x35 = 1225 points
gridDensity = 35; # The number of points in a "unit" (grid) or the entire area (recursive)

def buildConfig(baseName, numDev, algorithm, seed, txDim, rxDim, gridDensity)
	string = <<-ENDSTR
<edu.rutgers.winlab.junsim.Config>
\t<beta>0.65</beta>
\t<numTransmitters>#{numDev}</numTransmitters>
\t<numReceivers>#{numDev}</numReceivers>
\t<radioPower>2.0</radioPower>
\t<radioAlpha>2.68</radioAlpha>
\t<squareWidth>#{txDim[0]}</squareWidth>
\t<squareHeight>#{txDim[1]}</squareHeight>
\t<universeWidth>#{rxDim[0]}</universeWidth>
\t<universeHeight>#{rxDim[1]}</universeHeight>
\t<randomSeed>#{seed}</randomSeed>
\t<numTrials>1</numTrials>
\t<outputFileName>#{baseName}.csv</outputFileName>
\t<numThreads>0</numThreads>
\t<maxRangeMeters>40</maxRangeMeters>
\t<experimentType>#{algorithm}</experimentType>
\t<gridDensity>#{gridDensity}</gridDensity>
\t<randomized>false</randomized>
\t<renderConfig>graphics.xml</renderConfig>
\t<transmittersFile>../transmitters-#{numDev}.ssv</transmittersFile>
\t<receiversFile>receivers.ssv</receiversFile>
\t<outputBasePath>#{baseName}</outputBasePath>
</edu.rutgers.winlab.junsim.Config>
ENDSTR
end # buildConfig


for numTx in txSizes do
	for alg in algorithms do
		for seed in seeds do
			expName = "#{alg}-t#{numTx}-s#{seed}";
			configString = buildConfig(expName, numTx, alg, seed, txDimension, rxDimension, gridDensity);
			File.open("#{expName}.xml", 'w') { |file|
				file.write(configString);
				file.close;
			}
			`java -jar jun-sim.jar #{expName}.xml`
			pid = $?.pid
			success = $?.exitstatus
			if success != 0 
				puts "ERROR: Execution failed for #{expName}"
				exit
			end
		end # seeds
	end # algorithms
end # tx sizes


