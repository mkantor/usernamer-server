APP_SERVER_IP = "192.168.255.1"
MONGO_SERVER_IP = "192.168.255.2"
KITCHEN_ROOT = "../../../Kabam/kitchen"

chef_config = {
  :cookbooks_path => [ "#{KITCHEN_ROOT}/cookbooks" ],
  :data_bags_path => "#{KITCHEN_ROOT}/data_bags",
  :roles_path => "#{KITCHEN_ROOT}/roles",
}

# TODO: maybe make this a plugin
def give_vm_resources(vm, options = {})
  # scale VM memory/CPUs based on the host's resources
  unless RUBY_PLATFORM.downcase.include?("mswin")
    vm.provider :virtualbox do |virtualbox|
      if options[:memory_scale]
        host_memory_bytes = `
          if command -v free > /dev/null 2>&1
          then
            free -b | awk '/^Mem/ {print $2}'
          else
            sysctl hw.memsize | awk '{print $2}'
          fi
        `.to_i
        host_memory_mb = host_memory_bytes / 1024 / 1024
        vm_memory_mb = (host_memory_mb * options[:memory_scale]).floor
        if vm_memory_mb > 0
          virtualbox.customize ["modifyvm", :id, "--memory", vm_memory_mb.to_s]
        end
      end

      if options[:cpu_scale]
        host_cpus = `
          if [ -f /proc/cpuinfo ]
          then
            awk '/^processor/ {++n} END {print n}' /proc/cpuinfo
          else
            sysctl hw.logicalcpu | awk '{print $2}'
          fi
        `.to_i
        vm_cpus = (host_cpus * options[:cpu_scale]).floor
        if vm_cpus > 1
          # I/O APIC has to be enabled for the VM to use more than one cpu
          virtualbox.customize ["modifyvm", :id, "--ioapic", "on"]
          virtualbox.customize ["modifyvm", :id, "--cpus", vm_cpus.to_s]
        end
      end
    end
  end
end


Vagrant.configure("2") do |config|
  # for convenience we use a box with omnibus chef 11 pre-baked, but the prod images could also be used
  config.vm.box = "precise64-cloud"
  config.vm.box_url = "http://grahamc.com/vagrant/ubuntu-12.04-omnibus-chef.box"

  config.vm.define :app do |app|
    app.vm.provision :chef_solo do |chef|
      chef_config.each do |option, value|
        chef.send("#{option}=", value)
      end

      chef.add_role "appserver"
    end

    app.vm.network :private_network, :ip => APP_SERVER_IP
    
    # FIXME: I don't think i needed this with the base kws stuff... why do I need it now?
    app.vm.network :forwarded_port, :guest => 9000, :host => 9000

    give_vm_resources(app.vm, { :memory_scale => 0.25, :cpu_scale => 1 })
  end

  config.vm.define :mongo do |mongo|
    mongo.vm.provision :chef_solo do |chef|
      chef_config.each do |option, value|
        chef.send("#{option}=", value)
      end

      chef.add_role "mongocluster"
    end

    mongo.vm.network :private_network, :ip => MONGO_SERVER_IP

    give_vm_resources(mongo.vm, { :memory_scale => 0.25, :cpu_scale => 1 })
  end
end