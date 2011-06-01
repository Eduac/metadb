#!/bin/bash

RVM_FUNCTION='[[ -s "$HOME/.rvm/scripts/rvm" ]] && . "$HOME/.rvm/scripts/rvm" # Load RVM function'
RUBY_VERSION="1.9.2"
SETUP_PASSENGER=0
RVM_LIB_PATH=$rvm_path/usr

initial_setup() {
	sudo apt-get -y install git-all curl wget build-essential zlib1g zlib1g-dev libxml2 libxml2-dev libxslt-dev libopenssl-ruby libcurl4-openssl-dev libssl-dev
}


setup_ruby() {
        echo "Setting up Ruby on Rails"
	cd /usr/local/src
	wget http://ftp.ruby-lang.org/pub/ruby/1.9/ruby-1.9.2-p180.tar.gz
	tar -xvf ruby-1.9.2-p180.tar.gz
	cd ruby-1.9.2-p180
	./configure
	make
	make install
	cd ext/openssl
	ruby extconf.rb
	make 
	make install
}

setup_rails_passenger() {
	gem install rails
	gem install passenger
	passenger-install-nginx-module
	adduser --system --no-create-home --disabled-login --disabled-password --group nginx
}




helptext() {
	echo `basename $0`
        echo "-v <ruby_version> Ruby version, defaults to 1.9.2"
        echo "-h        Help"
}

while getopts ":v h p" opt; do
        case $opt in
                v) 
                        RUBY_VERSION=$OPTARG
                        ;;
		p|-passenger)
			SETUP_PASSENGER=1
			;;
                h|-help)
                        helptext
                        exit 0
                        ;;
        esac
done



# Check for necessary tools
initial_setup
if [ -z `which ruby` ]; then
	setup_ruby
	setup_rails_passenger
fi
if [ $SETUP_PASSENGER -eq 1 ]; then
	echo "Setup Passenger"
fi

